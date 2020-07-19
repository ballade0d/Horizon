package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.AutoClickerData;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.file.node.AutoClickerNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Async;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.collect.Pair;

import java.util.List;

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
        event.setCancelled(true);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final AutoClickerData data, final AutoClickerNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    private void typeA(final Event event, final HoriPlayer player, final AutoClickerData data, final AutoClickerNode config) {
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            long currTick = Async.currentTick;
            long ticks = currTick - data.lastHitTick;

            if (ticks < 5) {
                data.samplesA.add(ticks);
            }

            data.lastHitTick = currTick;

            if (data.samplesA.size() == 20) {
                Pair<List<Double>, List<Double>> outlierPair = MathUtils.getOutliers(data.samplesA);

                int outliers = outlierPair.key.size() + outlierPair.value.size();
                int duplicates = (int) (data.samplesA.size() - data.samplesA.stream().distinct().count());

                if (outliers < 2 && duplicates > 15) {
                    this.punish(event, player, data, 0, 2, "o:" + outliers, "d:" + duplicates);
                }

                data.samplesA.clear();
            }
        }
    }
}