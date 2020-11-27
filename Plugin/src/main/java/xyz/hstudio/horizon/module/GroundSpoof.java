package xyz.hstudio.horizon.module;

import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;

public class GroundSpoof extends CheckBase {

    public GroundSpoof(HPlayer p) {
        super(p, 1, 40, 40);
    }

    @Override
    public void received(InEvent<?> event) {
        if (event instanceof MoveEvent) {
            check((MoveEvent) event);
        }
    }

    private void check(MoveEvent e) {
        if (e.onGroundReally || e.step || e.teleport) {
            return;
        }
        /*
        if (e.onGroundReally || player.currentTick < 20 || player.vehicleBypass || e.stepLegitly ||
                e.isTeleport || e.piston || !e.collidingEntities.isEmpty() || player.isFlying()) {
            return
        }
         */
        if (e.onGround) {
            // Do another check to make sure if player is really not on ground
            // to avoid some false positives.
            if (e.to.isOnGround(p, false, 0.3)) {
                return;
            }

            Location checkLoc = new Location(e.to.world, p.physics.position.x, e.to.y, p.physics.position.z);
            if (checkLoc.isOnGround(p, false, 0.3)) {
                return;
            }

            AABB aabb = AABB.player().expand(0.0, -0.0001, 0.0).add(e.to);
            if (aabb.getBlockAABBs(p, p.getWorld(), Material.WEB).isEmpty()) {
                return;
            }

            // TODO: Check for client blocks

            e.modify(x -> MoveEvent.ACCESS.set(x, 5, false));

            System.out.println("GroundSpoof");
        }
    }
}
