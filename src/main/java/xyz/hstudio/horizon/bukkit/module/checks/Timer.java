package xyz.hstudio.horizon.bukkit.module.checks;

import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.TimerConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.TimerData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;

public class Timer extends Module<TimerData, TimerConfig> {

    public Timer() {
        super(ModuleType.Timer, new TimerConfig());
    }

    @Override
    public TimerData getData(final HoriPlayer player) {
        return player.timerData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final TimerData data, final TimerConfig config) {
        typeA(event, player, data, config);
    }

    /**
     * A basic Timer check.
     * <p>
     * Accuracy: 8/10 - It may have some false positives.
     * Efficiency: 10/10 - Detects 1.01 timer almost instantly.
     *
     * @author Islandscout, MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final TimerData data, final TimerConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            // Use nano second for more accuracy.
            long time = System.nanoTime();
            long timeElapsed = time - data.prevMoveTime;
            data.prevMoveTime = time;

            // Skip if player is teleporting.
            if (e.isTeleport || System.currentTimeMillis() - player.teleportTime < 2000L) {
                data.drift = 50000000L;
                return;
            }

            long drift = data.drift;
            long delta = timeElapsed - 50000000L;
            drift += delta;

            if (drift > 1000000 * 1000) {
                drift = 1000000 * 1000;
            }

            double diff = drift * 1E-6;
            // Player is 1 tick faster.
            // Also allowed network jitter.
            if (diff < -50) {
                this.debug("Failed: TypeA, d:" + diff + ", s:" + (-diff / 50));

                // Punish
                this.punish(player, data, "TypeA", -diff / 50 * 3);

                // Reset drift to -45 to stop spam-flagging.
                drift = -45 * 1000000;
            } else {
                reward("TypeA", data, 0.995);
            }
            // Reduce drift
            // Inspired by Islandscout, I'll credit him.
            if (drift < 0) {
                drift = (long) Math.min(0, drift + (50 - (50 / 1.005)) / 1E-6);
            } else {
                drift = (long) Math.max(0, drift + (50 - (50 / 0.995)) / 1E-6);
            }

            data.drift = drift;
        }
    }
}