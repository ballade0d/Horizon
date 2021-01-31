package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockBase;

import java.util.List;
import java.util.Set;

import static xyz.hstudio.horizon.util.Physics.AIR_RESISTANCE_VERTICAL;
import static xyz.hstudio.horizon.util.Physics.GRAVITATIONAL_ACCELERATION;

public class MoveEvent extends InEvent<PacketPlayInFlying> {

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

    public MoveEvent(HPlayer player, Location to, boolean onGround, boolean hasLook, boolean hasPos) {
        super(player);
        this.from = player.physics.position;
        this.to = to;
        this.onGround = onGround;
        this.hasLook = hasLook;
        this.hasPos = hasPos;
    }

    @Override
    public boolean pre() {
        this.velocity = to.minus(p.physics.position);

        this.teleport = testTeleport();

        this.newFriction = computeFriction();
        this.oldFriction = p.physics.friction;

        this.touchedFaces = new AABB(to.x - 0.299999, to.y + 0.000001, to.z - 0.299999, to.x + 0.299999, to.y + 1.799999, to.z + 0.299999).touchingFaces(p, to.world, 0.0001);
        this.touchedBlocks = AABB.player().expand(-0.001, -0.001, -0.001).add(velocity).getMaterials(to.world);

        this.touchCeiling = testTouchCeiling();
        this.onGroundReally = to.isOnGround(p, false, 0.001);
        this.step = testStep();
        this.jump = testJump();
        this.onSlime = testOnSlime();

        this.acceptedVelocity = testVelocity();
        this.knockBack = this.acceptedVelocity != null;

        return true;
    }

    private boolean testTeleport() {
        if (!p.teleport.teleporting) {
            return false;
        }
        if (!onGround && hasPos && hasLook && to.equals(p.teleport.location)) {
            inst.getAsync().clearHistory(p.base);
            return true;
        } else if (!p.bukkit.isSleeping()) {
            inst.getSync().teleport(p, p.teleport.location);
        }
        p.teleport.teleporting = false;
        return false;
    }

    private float computeFriction() {
        float friction = 0.91F;
        if (onGround) {
            Vector3D pos = p.physics.position;
            BlockBase b = new Location(to.world, pos.x, pos.y - 1, pos.z).getBlock();
            if (b != null) {
                friction *= b.friction();
            }
        }
        return friction;
    }

    private boolean testTouchCeiling() {
        Vector3D pos = p.physics.position.newY(to.y);
        AABB collisionBox = AABB.player().expand(-0.000001, -0.000001, -0.000001).add(pos);
        return collisionBox.touchingFaces(p, to.world, 0.0001).contains(Direction.UP);
    }

    private boolean testStep() {
        Vector3D prevPos = p.physics.position;
        Location extrapolate = to;
        // when on ground, Y velocity is inherently 0; no need to do pointless math.
        extrapolate.newY(extrapolate.y + (p.physics.onGroundReally ? -0.0784 :
                ((p.physics.velocity.y + GRAVITATIONAL_ACCELERATION) * AIR_RESISTANCE_VERTICAL)));

        AABB box = AABB.player().add(extrapolate);
        List<AABB> verticalCollision = box.getBlockAABBs(p, p.getWorld(), Material.WEB);

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
        box = AABB.player().add(to.newY(highestVertical)).expand(0, -0.00000000001, 0);

        List<AABB> horizontalCollision = box.getBlockAABBs(p, p.getWorld(), Material.WEB);

        if (horizontalCollision.isEmpty()) {
            return false;
        }

        double expectedY = prevPos.y;
        double highestPointOnAABB = -1;
        for (AABB blockAABB : horizontalCollision) {
            double blockAABBY = blockAABB.max.y;
            if (blockAABBY - prevPos.y > 0.6) {
                return false;
            }
            if (blockAABBY > expectedY) {
                expectedY = blockAABBY;
            }
            if (blockAABBY > highestPointOnAABB) {
                highestPointOnAABB = blockAABBY;
            }
        }

        return (onGround || onGroundReally) && Math.abs(prevPos.y - highestPointOnAABB) > 0.0001 &&
                Math.abs(to.y - expectedY) < 0.0001;
    }

    // Checks if the player's dY matches the expected dY
    private boolean testJump() {
        int jumpBoostLvl = p.getPotionAmplifier(PotionEffectType.JUMP);
        float expectedDY = Math.max(0.42f + jumpBoostLvl * 0.1f, 0f);
        boolean leftGround = p.physics.onGround && !onGround;
        float dY = (float) (to.y - p.physics.position.y);

        // Jumping right as you enter a 2-block-high space will not change your motY.
        // When these conditions are met, we'll give them the benefit of the doubt and say that they jumped.
        {
            AABB box = AABB.player();
            box.expand(-0.000001, -0.000001, -0.000001);
            box.add(to.plus(0, expectedDY, 0));
            boolean collidedNow = !box.getBlockAABBs(p, to.world).isEmpty();

            box = AABB.player();
            box.expand(-0.000001, -0.000001, -0.000001);
            box.add(p.physics.position.plus(0, expectedDY, 0));
            boolean collidedBefore = !box.getBlockAABBs(p, to.world).isEmpty();

            if (collidedNow && !collidedBefore && leftGround && dY == 0) {
                expectedDY = 0;
            }
        }

        Set<Material> touchedBlocks = p.base.cube(to).getMaterials(p.getWorld());
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
        float deltaY = (float) (to.y - p.physics.position.y);
        BlockBase standingOn = p.physics.position.plus(0, -0.2, 0).getBlock();
        if (standingOn == null || standingOn.type() != Material.SLIME_BLOCK) {
            return false;
        }
        float prevPrevDeltaY = (float) p.physics.prevVelocity.y;
        float expected = (-((prevPrevDeltaY - 0.08F) * 0.98F) - 0.08F) * 0.98F;
        // TODO: you're supposed to get the sneaking from the LAST tick, not this one
        return !p.status.isSneaking && p.physics.onGround && !onGround && deltaY >= 0 && Math.abs(expected - deltaY) < 0.001;
    }

    // Old velocity tester
    private Vector3D testVelocity() {
        if (p.velocity.x == 0 && p.velocity.y == 0 && p.velocity.z == 0) {
            return null;
        }
        double speedPotMultiplier = 1 + p.getPotionAmplifier(PotionEffectType.SPEED) * 0.2;
        double sprintMultiplier = p.status.isSprinting ? 1.3 : 1;
        double weirdConstant = jump && p.status.isSprinting ? 0.2518462 : 0.098;
        double baseMultiplier = 5 * p.bukkit.getWalkSpeed() * speedPotMultiplier;
        double maxDiscrepancy = weirdConstant * baseMultiplier * sprintMultiplier + 0.003;

        double x = p.status.hitSlowdown ? 0.6 * p.velocity.x : p.velocity.x;
        double y = p.velocity.y;
        double z = p.status.hitSlowdown ? 0.6 * p.velocity.z : p.velocity.z;

        if (!((touchedFaces.contains(Direction.UP) && y > 0) || (touchedFaces.contains(Direction.DOWN) && y < 0)) &&
                Math.abs(y - velocity.y) > 0.01 &&
                !jump && !step) {
            failedVelocity = true;
            return null;
        }

        double minX = x - maxDiscrepancy;
        double maxX = x + maxDiscrepancy;
        double minZ = z - maxDiscrepancy;
        double maxZ = z + maxDiscrepancy;
        if (!((touchedFaces.contains(Direction.EAST) && x > 0) || (touchedFaces.contains(Direction.WEST) && x < 0)) &&
                !(velocity.x <= maxX && velocity.x >= minX)) {
            failedVelocity = true;
            return null;
        }
        if (!((touchedFaces.contains(Direction.SOUTH) && z > 0) || (touchedFaces.contains(Direction.NORTH) && z < 0)) &&
                !(velocity.z <= maxZ && velocity.z >= minZ)) {
            failedVelocity = true;
            return null;
        }

        p.velocity.x = p.velocity.y = p.velocity.z = 0;

        return new Vector3D(x, y, z);
    }

    @Override
    public void post() {
        HPlayer.Physics physics = p.physics;

        physics.position = to;
        physics.wasOnGround = physics.onGround;
        physics.onGround = onGround;
        physics.onGroundReally = onGroundReally;
        physics.prevVelocity = physics.velocity;
        physics.velocity = velocity;
        physics.friction = newFriction;

        p.status.hitSlowdown = false;
    }
}