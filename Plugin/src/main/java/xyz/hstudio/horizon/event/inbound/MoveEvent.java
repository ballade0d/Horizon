package xyz.hstudio.horizon.event.inbound;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;

import java.util.List;
import java.util.Set;

import static xyz.hstudio.horizon.util.Physics.GRAVITATIONAL_ACCELERATION;

public class MoveEvent extends InEvent {

    public final Location to;
    public final boolean onGround;
    public final boolean hasLook;
    public final boolean hasPos;

    public Vector3D velocity;
    public Vector3D acceptedKnockback; // TODO finish this
    public boolean teleport;
    public boolean onGroundReally;
    public boolean touchCeiling;
    public boolean step;
    public boolean jump;

    public MoveEvent(HPlayer p, Location to, boolean onGround, boolean hasLook, boolean hasPos) {
        super(p);
        this.to = to;
        this.onGround = onGround;
        this.hasLook = hasLook;
        this.hasPos = hasPos;
    }

    @Override
    public boolean pre() {
        this.velocity = to.minus(p.physics.position);

        if (p.status.isTeleporting) {
            Location tpLoc;
            long elapsedTime;
            if (p.teleports.size() == 0) {
                tpLoc = null;
                elapsedTime = 0;
            } else {
                Pair<Location, Long> tpPair = p.teleports.get(0);
                tpLoc = tpPair.getKey();
                elapsedTime = System.currentTimeMillis() - tpPair.getValue();
            }

            if (!onGround && hasPos && hasLook && to.equals(tpLoc)) {
                p.physics.position = tpLoc;
                p.physics.velocity = new Vector3D(0, 0, 0);

                p.teleports.remove(0);

                this.teleport = true;
                inst.getAsync().clearHistory(p.base);

                if (p.teleports.size() == 0) p.status.isTeleporting = false;
                else return false;
            } else if (!p.bukkit.isSleeping()) {
                if (elapsedTime > p.status.ping + 800) {
                    Location tp;
                    if (p.teleports.size() > 0) {
                        tp = p.teleports.get(p.teleports.size() - 1).getKey();
                        p.teleports.clear();
                    } else tp = p.physics.position;
                    inst.getSync().teleport(p, tp);
                }
            }
        }
        this.touchCeiling = testTouchCeiling();
        this.onGroundReally = to.isOnGround(p, false, 0.001);
        this.step = testStep();
        this.jump = testJump();

        return super.pre();
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
                ((p.physics.velocity.y + GRAVITATIONAL_ACCELERATION) * 0.98)));

        AABB box = AABB.player().add(extrapolate);
        List<AABB> verticalCollision = box.getBlockAABBs(p, p.getWorld(), Material.WEB);

        if (verticalCollision.isEmpty() && !p.physics.onGround) return false;

        double highestVertical = extrapolate.y;
        for (AABB blockAABB : verticalCollision) {
            double aabbMaxY = blockAABB.max.y;
            if (aabbMaxY > highestVertical) highestVertical = aabbMaxY;
        }

        // move to this position, but with clipped Y (moving horizontally)
        box = AABB.player().add(to.newY(highestVertical)).expand(0, -0.00000000001, 0);

        List<AABB> horizontalCollision = box.getBlockAABBs(p, p.getWorld(), Material.WEB);

        if (horizontalCollision.isEmpty()) return false;

        double expectedY = prevPos.y;
        double highestPointOnAABB = -1;
        for (AABB blockAABB : horizontalCollision) {
            double blockAABBY = blockAABB.max.y;
            if (blockAABBY - prevPos.y > 0.6) return false;
            if (blockAABBY > expectedY) expectedY = blockAABBY;
            if (blockAABBY > highestPointOnAABB) highestPointOnAABB = blockAABBY;
        }

        return (onGround || onGroundReally) && Math.abs(prevPos.y - highestPointOnAABB) > 0.0001 && Math.abs(to.y - expectedY) < 0.0001;
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
            box.add(to.plus(new Vector3D(0, expectedDY, 0)));
            boolean collidedNow = !box.getBlockAABBs(p, to.world).isEmpty();

            box = AABB.player();
            box.expand(-0.000001, -0.000001, -0.000001);
            box.add(p.physics.position.plus(new Vector3D(0, expectedDY, 0)));
            boolean collidedBefore = !box.getBlockAABBs(p, to.world).isEmpty();

            if (collidedNow && !collidedBefore && leftGround && dY == 0) expectedDY = 0;
        }

        Set<Material> touchedBlocks = p.base.cube(to).getMaterials(p.getWorld());
        if (touchedBlocks.contains(Material.WEB)) {
            if (hasPos) expectedDY *= 0.05;
            else expectedDY = 0;
        }

        boolean kbSimilarToJump = acceptedKnockback != null && (Math.abs(acceptedKnockback.y - expectedDY) < 0.001 || touchCeiling);
        return !kbSimilarToJump && ((expectedDY == 0 && p.physics.onGround) || leftGround) && (dY == expectedDY || touchCeiling);
    }

    @Override
    public void post() {
        HPlayer.Physics physics = p.physics;

        physics.position = to;
        physics.onGround = onGround;
        physics.onGroundReally = onGroundReally;
        physics.prevVelocity = physics.velocity;
        physics.velocity = velocity;
        super.post();
    }
}