package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.GroundSpoofData;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.events.inbound.MoveEvent;
import xyz.hstudio.horizon.file.node.GroundSpoofNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;

public class GroundSpoof extends Module<GroundSpoofData, GroundSpoofNode> {

    public GroundSpoof() {
        super(ModuleType.GroundSpoof, new GroundSpoofNode(), "TypeA");
    }

    @Override
    public GroundSpoofData getData(final HoriPlayer player) {
        return player.groundSpoofData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final GroundSpoofData data, final GroundSpoofNode config) {
        McAccessor.INSTANCE.setOnGround((MoveEvent) event, false);
    }

    @Override
    public void doCheck(Event event, HoriPlayer player, GroundSpoofData data, GroundSpoofNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    /**
     * GroundSpoof check.
     * <p>
     * Accuracy: 9/10 - Only some really rare false positives.
     * Efficiency: 10/10 - Detects related hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final GroundSpoofData data, final GroundSpoofNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            // Only check if player is not on ground
            if (e.onGroundReally || player.currentTick < 20 || e.stepLegitly || e.isTeleport || e.piston || e.isCollidingEntities || player.isFlying()) {
                return;
            }
            // Player is not on ground but appears on ground.
            if (e.onGround) {
                // Do another check to make sure if player is really not on ground
                // to avoid some false positives.
                Location checkLoc = new Location(e.to.world, e.from.x, e.to.y, e.from.z);
                AABB aabb = AABB.NORMAL_BOX.expand(0, -0.0001, 0).translate(e.to.toVector());
                if (aabb.getBlockAABBs(player, player.world, MatUtils.COBWEB.parse()).isEmpty() &&
                        checkLoc.isOnGround(player, false, 0.3)) {
                    return;
                }
                if (e.to.isOnGround(player, false, 0.3)) {
                    return;
                }
                if (e.clientBlock != -1) {
                    return;
                }
                e.onGround = false;

                // Punish
                this.punish(event, player, data, 0, 3);
            } else {
                reward(0, data, 0.999);
            }
        }
    }
}