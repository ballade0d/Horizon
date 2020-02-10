package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.config.checks.AntiVelocityConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.AntiVelocityData;
import xyz.hstudio.horizon.module.Module;

public class AntiVelocity extends Module<AntiVelocityData, AntiVelocityConfig> {

    public AntiVelocity() {
        super(ModuleType.AntiVelocity, new AntiVelocityConfig());
    }

    @Override
    public AntiVelocityData getData(final HoriPlayer player) {
        return player.antiVelocityData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final AntiVelocityData data, final AntiVelocityConfig config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final AntiVelocityData data, final AntiVelocityConfig config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    /**
     * A simple AntiVelocity check.
     * <p>
     * Accuracy: 8/10 - May have some false positives.
     * Efficiency: 10/10 - Detects related hacks really fast.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final AntiVelocityData data, final AntiVelocityConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.failedKnockBack) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(event, player, data, "TypeA", 4);
            } else {
                reward("TypeA", data, 0.999);
            }
        }
    }
}