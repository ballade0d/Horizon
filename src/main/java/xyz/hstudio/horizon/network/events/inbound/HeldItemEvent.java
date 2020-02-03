package xyz.hstudio.horizon.network.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class HeldItemEvent extends Event {

    public final int slot;

    public HeldItemEvent(final HoriPlayer player, final int slot, final WrappedPacket packet) {
        super(player, packet);
        this.slot = slot;
    }

    @Override
    public boolean pre() {
        player.heldSlot = this.slot;
        return true;
    }
}