package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.AutoClickerData;
import xyz.hstudio.horizon.file.node.AutoClickerNode;
import xyz.hstudio.horizon.module.Module;

public class AutoClicker extends Module<AutoClickerData, AutoClickerNode> {

    public AutoClicker() {
        super(ModuleType.AutoClicker, new AutoClickerNode());
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
    }
}