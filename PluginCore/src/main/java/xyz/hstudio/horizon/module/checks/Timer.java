package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.TimerData;
import xyz.hstudio.horizon.file.node.TimerNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;

public class Timer extends Module<TimerData, TimerNode> {

    private static final long MULTIPLIER = (long) 1E6;

    public Timer() {
        super(ModuleType.Timer, new TimerNode(), "TypeA");
    }

    @Override
    public TimerData getData(final HoriPlayer player) {
        return player.timerData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final TimerData data, final TimerNode config) {
        event.setCancelled(true);
        Sync.teleport(player, player.position);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final TimerData data, final TimerNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    /**
     * A basic Timer check.
     * <p>
     * Accuracy: 8/10 - It may have some false positives.
     * Efficiency: 10/10 - Detects 1.01 timer almost instantly.
     *
     * @author Islandscout, MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final TimerData data, final TimerNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            // Use nano second for more accuracy.
            long time = System.nanoTime();
            long timeElapsed = time - data.prevMoveTime;
            data.prevMoveTime = time;

            // Skip if player is teleporting.
            // Ignore the first 2 second joining the server.
            if (e.isTeleport || System.currentTimeMillis() - player.lastTeleportTime < 1000L || player.currentTick < 40) {
                data.drift = 0;
                return;
            }

            long drift = data.drift;
            long delta = timeElapsed - 50 * MULTIPLIER;
            drift += delta;

            if (drift > 1000 * MULTIPLIER) {
                drift = 1000 * MULTIPLIER;
            }

            double diff = drift * 1E-6;
            // Player is 1 tick faster.
            // Also allowed network jitter.
            if (diff < -config.typeA_allow_ms) {
                if (++data.fails > 4) {
                    // Punish
                    this.punish(event, player, data, 0, (float) (-diff / 50F * 2F),
                            "d:" + diff, "s:" + (-diff / 50));

                    // Reset drift to -45 to stop spam-flagging.
                    drift = -45 * MULTIPLIER;
                }
            } else if (data.fails > 0) {
                data.fails--;
            } else {
                reward(0, data, 0.995);
            }
            // Reduce drift
            // Inspired by Islandscout, I'll credit him.
            if (drift < 0) {
                drift = (long) Math.min(0, drift + (50 - (50 / 1.02)) * MULTIPLIER);
            } else {
                drift = (long) Math.max(0, drift + (50 - (50 / 0.98)) * MULTIPLIER);
            }

            data.drift = drift;
        }
    }
}