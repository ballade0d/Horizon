package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.node.ESPNode;
import xyz.hstudio.horizon.module.Module;

public class ESP extends Module<Data, ESPNode> {

    public ESP() {
        super(ModuleType.ESP, new ESPNode());
    }

    @Override
    public Data getData(final HoriPlayer player) {
        return null;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final Data data, final ESPNode config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final Data data, final ESPNode config) {
    }

    @Override
    public void tickAsync(final long currentTick, final ESPNode config) {

    }
}