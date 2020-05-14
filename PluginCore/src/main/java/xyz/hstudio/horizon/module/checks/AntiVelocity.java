package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.AntiVelocityData;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.events.inbound.MoveEvent;
import xyz.hstudio.horizon.file.node.AntiVelocityNode;
import xyz.hstudio.horizon.module.Module;

public class AntiVelocity extends Module<AntiVelocityData, AntiVelocityNode> {

    public AntiVelocity() {
        super(ModuleType.AntiVelocity, new AntiVelocityNode(), "TypeA");
    }

    @Override
    public AntiVelocityData getData(final HoriPlayer player) {
        return player.antiVelocityData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final AntiVelocityData data, final AntiVelocityNode config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final AntiVelocityData data, final AntiVelocityNode config) {
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
    private void typeA(final Event event, final HoriPlayer player, final AntiVelocityData data, final AntiVelocityNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (player.teleports.size() > 0) {
                return;
            }
            if (e.failedKnockBack && !e.isTeleport) {
                // Punish
                this.punish(event, player, data, 0, 4);
            } else {
                reward(0, data, 0.999);
            }
        }
    }
}