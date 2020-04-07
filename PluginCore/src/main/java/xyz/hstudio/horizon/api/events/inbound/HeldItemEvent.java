package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

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