package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockWrapper;

import java.util.Set;

public class Strafe extends CheckBase {

    private static final double THRESHOLD = Math.toRadians(0.5);

    private boolean bounced;
    private boolean wasSneakingOnEdge;

    public Strafe(HPlayer p) {
        super(p, 1, 20, 20);
    }

    @Override
    public void run(Event<?> event) {
        if (event instanceof MoveEvent) {
            angle((MoveEvent) event);
        }
    }

    private void angle(MoveEvent e) {
        BlockWrapper footBlock = p.physics.position.add(0, -0.2, 0).getBlock();
        if (footBlock == null) {
            return;
        }

        boolean collidingHorizontally = collidingHorizontally(e);
        boolean sneakEdge = p.status.isSneaking && !BlockUtils.isSolid(footBlock) && e.onGround;

        Set<Material> activeBlocks = e.touchedBlocks;

        boolean onSlimeBlock = p.physics.onGround && footBlock.type() == Material.SLIME_BLOCK;
        boolean nearLiquid = testLiquid(activeBlocks);

        if (e.teleport || e.knockBack || bounced || collidingHorizontally || !e.hasPos ||
                sneakEdge || e.jump || nearLiquid || activeBlocks.contains(Material.LADDER) ||
                activeBlocks.contains(Material.VINE) || wasSneakingOnEdge || onSlimeBlock ||
                (e.step && p.status.isSprinting) || (!e.onGround && e.onGroundReally)) {
            this.wasSneakingOnEdge = sneakEdge;
            return;
        }

        double friction = e.oldFriction;

        Vector3D prevVelocity = p.physics.velocity.clone();
        if (p.status.hitSlowdown) {
            prevVelocity.multiply(0.6);
        }

        for (Material material : activeBlocks) {
            if (material == Material.SOUL_SAND) {
                prevVelocity.multiply(0.4);
            }
            if (material == Material.WEB) {
                prevVelocity.multiply(0);
                // Any number times 0 is 0; no reason to continue looping.
                break;
            }
        }

        if (Math.abs(prevVelocity.x * friction) < 0.005) {
            prevVelocity.x = 0;
        }
        if (Math.abs(prevVelocity.z * friction) < 0.005) {
            prevVelocity.z = 0;
        }

        double dX = e.to.x - e.from.x;
        double dZ = e.to.z - e.from.z;
        dX /= friction;
        dZ /= friction;
        dX -= prevVelocity.x;
        dZ -= prevVelocity.z;

        Vector3D accelDir = new Vector3D(dX, 0, dZ);
        Vector3D yaw = MathUtils.getDirection(e.to.yaw, 0);

        // You aren't pressing a WASD key
        if (accelDir.lengthSquared() < 0.000001) {
            this.wasSneakingOnEdge = false;
            return;
        }

        boolean vectorDir = accelDir.crossProduct(yaw).dot(new Vector3D(0, 1, 0)) >= 0;
        double angle = (vectorDir ? 1 : -1) * accelDir.angle(yaw);

        if (!isValidStrafe(angle)) {
            punish(e, "Strafe (oeIuf)", 1, Detection.STRAFE, "a:" + angle);
        }

        this.wasSneakingOnEdge = false;
    }

    private boolean collidingHorizontally(MoveEvent e) {
        for (Direction dir : e.touchedFaces) {
            if (dir == Direction.EAST || dir == Direction.NORTH ||
                    dir == Direction.SOUTH || dir == Direction.WEST) {
                bounced = true;
                return true;
            }
        }
        bounced = false;
        return false;
    }

    private static boolean testLiquid(Set<Material> mats) {
        for (Material mat : mats) {
            if (mat == Material.WATER || mat == Material.STATIONARY_WATER || mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidStrafe(double angle) {
        double modulo = (angle % (Math.PI / 4)) * (4 / Math.PI);
        double error = Math.abs(modulo - Math.round(modulo)) * (Math.PI / 4);
        return error <= THRESHOLD;
    }
}