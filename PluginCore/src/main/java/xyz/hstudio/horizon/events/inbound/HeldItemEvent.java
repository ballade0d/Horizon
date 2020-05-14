package xyz.hstudio.horizon.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public class HeldItemEvent extends Event {

    public final int slot;

    public HeldItemEvent(final HoriPlayer player, final int slot) {
        super(player);
        this.slot = slot;
    }

    @Override
    public boolean pre() {
        player.heldSlot = this.slot;
        player.isEating = false;
        player.isPullingBow = false;
        player.isBlocking = false;
        return true;
    }
}