package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.AutoClickerData;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.file.node.AutoClickerNode;
import xyz.hstudio.horizon.module.Module;

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
            // TODO: Recode this
            // typeA(event, player, data, config);
        }
    }
}