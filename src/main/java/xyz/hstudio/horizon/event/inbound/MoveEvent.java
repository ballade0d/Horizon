package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.MobEffectList;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockWrapper;

import java.util.List;
import java.util.Set;

public class MoveEvent extends Event<PacketPlayInFlying> {

    public final Location to;
    public final Location from;
    public final boolean onGround;
    public final boolean hasLook;
    public final boolean hasPos;

    public Vector3D velocity;

    /*
     *  Teleport
     */
    public boolean teleport;
    /*
     *  Status
     */
    public Set<Direction> touchedFaces;
    public Set<Material> touchedBlocks;
    public boolean touchCeiling;
    public boolean onGroundReally;
    public boolean step;
    public boolean jump;
    public boolean onSlime;
    /*
     *  Velocity
     */
    public Vector3D acceptedVelocity;
    public boolean knockBack;
    public boolean failedVelocity;
    /*
     * Values
     */
    //This is the friction that is used to compute this move's initial force.
    private float newFriction;
    //This is the friction that affects this move's velocity.
    private float oldFriction;

    public MoveEvent(HPlayer p, Location to, boolean onGround, boolean hasLook, boolean hasPos) {
        super(p);
        this.from = p.physics.position;
        this.to = to;
        this.onGround = onGround;
        this.hasLook = hasLook;
        this.hasPos = hasPos;
    }

    @Override
    public boolean pre() {
        p.currTick++;

        this.velocity = to.minus(from);

        this.teleport = testTeleport();

        this.newFriction = computeFriction();
        this.oldFriction = p.physics.friction;

        this.touchedFaces = new AABB(to.x - 0.299999, to.y + 0.000001, to.z - 0.299999, to.x + 0.299999, to.y + 1.799999, to.z + 0.299999).touchingFaces(p, to.world, 0.0001);
        this.touchedBlocks = to.toAABB().expand(-0.001, -0.001, -0.001).materials(to.world);

        this.touchCeiling = testTouchCeiling();
        this.onGroundReally = to.onGround(p, false, 0.02);
        this.step = testStep();
        this.jump = testJump();
        this.onSlime = testOnSlime();

        this.acceptedVelocity = testVelocity();
        this.knockBack = this.acceptedVelocity != null;

        boolean glidingInUnloadedChunk = Math.abs(velocity.y - -0.098) < 0.0000001 && (acceptedVelocity == null || Math.abs(acceptedVelocity.y - -0.098) > 0.0000001);
        if (!teleport && glidingInUnloadedChunk) {
            inst.getSync().teleport(p, from);
            return false;
        }
        return true;
    }

    private boolean testTeleport() {
        if (!p.teleport.teleporting) {
            return false;
        }
        if (!onGround && hasPos && hasLook && to.equals(p.teleport.location)) {
            inst.getAsync().clearHistory(p.base);
            p.teleport.teleporting = false;
            return true;
        } else if (!p.nms.isSleeping()) {
            inst.getSync().teleport(p, p.teleport.location);
        }
        p.teleport.teleporting = false;
        return false;
    }

    private float computeFriction() {
        float friction = 0.91F;
        if (onGround) {
            BlockWrapper b = new Location(to.world, from.x, from.y - 1, from.z).getBlock();
            if (b != null) {
                friction *= b.friction();
            }
        }
        return friction;
    }

    private boolean testTouchCeiling() {
        Vector3D pos = from.newY(to.y);
        AABB collisionBox = pos.toAABB().expand(-0.000001, -0.000001, -0.000001);
        return collisionBox.touchingFaces(p, to.world, 0.0001).contains(Direction.UP);
    }

    private boolean testStep() {
        Location extrapolate = to;
        // when on ground, Y velocity is inherently 0; no need to do pointless math.
        extrapolate.newY(extrapolate.y + (p.physics.onGroundReally ? -0.0784 :
                ((p.physics.velocity.y - 0.08) * 0.98)));

        AABB box = extrapolate.toAABB();
        List<AABB> verticalCollision = box.getBlockAABBs(p, p.world(), Material.WEB);

        if (verticalCollision.isEmpty() && !p.physics.onGround) {
            return false;
        }

        double highestVertical = extrapolate.y;
        for (AABB blockAABB : verticalCollision) {
            double aabbMaxY = blockAABB.max.y;
            if (aabbMaxY > highestVertical) {
                highestVertical = aabbMaxY;
            }
        }

        // move to this position, but with clipped Y (moving horizontally)
        box = to.newY(highestVertical).toAABB().expand(0, -0.00000000001, 0);

        List<AABB> horizontalCollision = box.getBlockAABBs(p, p.world(), Material.WEB);

        if (horizontalCollision.isEmpty()) {
            return false;
        }

        double expectedY = from.y;
        double highestPointOnAABB = -1;
        for (AABB blockAABB : horizontalCollision) {
            double blockAABBY = blockAABB.max.y;
            if (blockAABBY - from.y > 0.6) {
                return false;
            }
            if (blockAABBY > expectedY) {
                expectedY = blockAABBY;
            }
            if (blockAABBY > highestPointOnAABB) {
                highestPointOnAABB = blockAABBY;
            }
        }

        return (onGround || onGroundReally) && Math.abs(from.y - highestPointOnAABB) > 0.0001 &&
                Math.abs(to.y - expectedY) < 0.0001;
    }

    // Checks if the player's dY matches the expected dY
    private boolean testJump() {
        int jumpBoostLvl = p.getEffectAmplifier(MobEffectList.JUMP);
        float expectedDY = Math.max(0.42f + jumpBoostLvl * 0.1f, 0f);
        boolean leftGround = p.physics.onGround && !onGround;
        float dY = (float) (to.y - from.y);

        // Jumping right as you enter a 2-block-high space will not change your motY.
        // When these conditions are met, we'll give them the benefit of the doubt and say that they jumped.
        {
            AABB box = AABB.def();
            box.expand(-0.000001, -0.000001, -0.000001);
            box.add(to.plus(0, expectedDY, 0));
            boolean collidedNow = !box.getBlockAABBs(p, to.world).isEmpty();

            box = AABB.def();
            box.expand(-0.000001, -0.000001, -0.000001);
            box.add(from.plus(0, expectedDY, 0));
            boolean collidedBefore = !box.getBlockAABBs(p, to.world).isEmpty();

            if (collidedNow && !collidedBefore && leftGround && dY == 0) {
                expectedDY = 0;
            }
        }

        Set<Material> touchedBlocks = p.base.cube(to).materials(p.world());
        if (touchedBlocks.contains(Material.WEB)) {
            if (hasPos) {
                expectedDY *= 0.05;
            } else {
                expectedDY = 0;
            }
        }

        boolean kbSimilarToJump = acceptedVelocity != null &&
                (Math.abs(acceptedVelocity.y - expectedDY) < 0.001 || touchCeiling);
        return !kbSimilarToJump &&
                ((expectedDY == 0 && p.physics.onGround) || leftGround) &&
                (dY == expectedDY || touchCeiling);
    }

    private boolean testOnSlime() {
        float deltaY = (float) (to.y - from.y);
        BlockWrapper standingOn = from.plus(0, -0.2, 0).getBlock();
        if (standingOn == null || standingOn.type() != Material.SLIME_BLOCK) {
            return false;
        }
        float prevPrevDeltaY = (float) p.physics.oldVelocity.y;
        float expected = (-((prevPrevDeltaY - 0.08F) * 0.98F) - 0.08F) * 0.98F;
        // TODO: you're supposed to get the sneaking from the LAST tick, not this one
        return !p.status.isSneaking && p.physics.onGround && !onGround && deltaY >= 0 && Math.abs(expected - deltaY) < 0.001;
    }

    // Old velocity tester
    private Vector3D testVelocity() {
        if (p.velocity.x == 0 && p.velocity.y == 0 && p.velocity.z == 0) {
            return null;
        }
        // This should fix any false positives caused by network lags.
        if (p.currTick - p.velocity.receivedTick > 5) {
            p.velocity.x = p.velocity.y = p.velocity.z = 0;
            failedVelocity = true;
            return null;
        }
        double speedPotMultiplier = 1 + p.getEffectAmplifier(MobEffectList.FASTER_MOVEMENT) * 0.2;
        double sprintMultiplier = p.status.isSprinting ? 1.3 : 1;
        double weirdConstant = jump && p.status.isSprinting ? 0.2518462 : 0.098;
        double baseMultiplier = 5 * p.walkSpeed() * speedPotMultiplier;
        double maxDiscrepancy = weirdConstant * baseMultiplier * sprintMultiplier + 0.003;

        double x = p.status.hitSlowdown ? 0.6 * p.velocity.x : p.velocity.x;
        double y = p.velocity.y;
        double z = p.status.hitSlowdown ? 0.6 * p.velocity.z : p.velocity.z;

        if (!((touchedFaces.contains(Direction.UP) && y > 0) || (touchedFaces.contains(Direction.DOWN) && y < 0)) &&
                Math.abs(y - velocity.y) > 0.01 &&
                !jump && !step) {
            p.sendMessage("A");
            return null;
        }

        double minX = x - maxDiscrepancy;
        double maxX = x + maxDiscrepancy;
        double minZ = z - maxDiscrepancy;
        double maxZ = z + maxDiscrepancy;
        if (!((touchedFaces.contains(Direction.EAST) && x > 0) || (touchedFaces.contains(Direction.WEST) && x < 0)) &&
                !(velocity.x <= maxX && velocity.x >= minX)) {
            p.sendMessage("B");
            return null;
        }
        if (!((touchedFaces.contains(Direction.SOUTH) && z > 0) || (touchedFaces.contains(Direction.NORTH) && z < 0)) &&
                !(velocity.z <= maxZ && velocity.z >= minZ)) {
            p.sendMessage("C");
            return null;
        }

        p.velocity.x = p.velocity.y = p.velocity.z = 0;

        return new Vector3D(x, y, z);
    }

    @Override
    public void post() {
        HPlayer.Physics physics = p.physics;

        physics.position = to;
        physics.onGround = onGround;
        physics.onGroundReally = onGroundReally;
        physics.oldVelocity = physics.velocity;
        physics.velocity = velocity;
        physics.friction = newFriction;

        physics.touchedFaces = touchedFaces;

        p.status.hitSlowdown = false;
    }
}