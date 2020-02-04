package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.config.checks.GroundSpoofConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.GroundSpoofData;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.wrap.Location;

public class GroundSpoof extends Module<GroundSpoofData, GroundSpoofConfig> {

    public GroundSpoof() {
        super(ModuleType.GroundSpoof, new GroundSpoofConfig());
    }

    @Override
    public GroundSpoofData getData(final HoriPlayer player) {
        return player.groundSpoofData;
    }

    @Override
    public void doCheck(Event event, HoriPlayer player, GroundSpoofData data, GroundSpoofConfig config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    /**
     * A simple GroundSpoof check.
     * <p>
     * Accuracy: 9/10 - Only some really rare false positives.
     * Efficiency: 10/10 - Detects related hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final GroundSpoofData data, final GroundSpoofConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            // Only check if player is not on ground
            if (e.onGroundReally) {
                return;
            }
            // Player is not on ground but appears on ground.
            if (e.onGround) {
                Location checkLoc = new Location(e.to.world, e.from.x, e.to.y, e.from.z);
                // Do another check to make sure if player is really not on ground
                // to avoid some false positives.
                if (checkLoc.isOnGround(false, 0.025)) {
                    return;
                }
                if (e.clientBlock != null) {
                    return;
                }
                e.onGround = false;

                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 4);
            } else {
                reward("TypeA", data, 0.999);
            }
        }
    }
}