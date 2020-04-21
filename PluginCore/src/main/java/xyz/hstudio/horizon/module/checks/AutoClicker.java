package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.AutoClickerData;
import xyz.hstudio.horizon.file.node.AutoClickerNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Async;
import xyz.hstudio.horizon.util.MathUtils;

import java.util.List;
import java.util.stream.Collectors;

public class AutoClicker extends Module<AutoClickerData, AutoClickerNode> {

    public AutoClicker() {
        super(ModuleType.AutoClicker, new AutoClickerNode(), "TypeA");
    }

    @Override
    public AutoClickerData getData(final HoriPlayer player) {
        return player.autoClickerData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final AutoClickerData data, final AutoClickerNode config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final AutoClickerData data, final AutoClickerNode config) {
        typeA(event, player, data, config);
    }

    private void typeA(final Event event, final HoriPlayer player, final AutoClickerData data, final AutoClickerNode config) {
        if (event instanceof InteractEntityEvent) {
            long currTick = Async.currentTick;
            long deltaTick = currTick - data.prevHitTick;

            List<Long> deltaTicks = data.deltaTicks;
            if (deltaTick < (int) (20 * (1 / config.typeA_min_encounter_cps) + 1)) {
                deltaTicks.add(deltaTick);

                if (deltaTicks.size() >= config.typeA_sample_size) {
                    double avgCps = 0;
                    for (long d : deltaTicks) {
                        avgCps += 1D / (d / 20D);
                    }
                    avgCps /= deltaTicks.size();

                    List<Double> hitSamples = data.hitSamples;
                    hitSamples.add(avgCps);

                    avgCps = hitSamples.stream().collect(Collectors.averagingDouble(Double::doubleValue));
                    if (hitSamples.size() >= config.typeA_samples && avgCps > config.typeA_min_check_cps) {
                        double stdev = MathUtils.standardDeviation(hitSamples.toArray(new Double[0]));
                        if (stdev < config.typeA_stdev) {
                            // Punish
                            this.punish(event, player, data, 0, 1, "cps:" + avgCps, "stdev:" + stdev);
                        }
                    }

                    if (hitSamples.size() >= 5) {
                        hitSamples.remove(0);
                    }

                    deltaTicks.clear();
                }
            }
            data.prevHitTick = currTick;
        }
    }
}