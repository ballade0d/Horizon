package xyz.hstudio.horizon.event.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

public class BlockUpdateEvent extends Event {

    public final IWrappedBlock block;

    public BlockUpdateEvent(final HoriPlayer player, final IWrappedBlock block) {
        super(player);
        this.block = block;
    }

    @Override
    public void post() {
    }
}