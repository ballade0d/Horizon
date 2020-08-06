package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class HeldItemEvent extends Event {

    public final int slot;

    public HeldItemEvent(final HoriPlayer player, final int slot) {
        super(player);
        this.slot = slot;
    }

    @Override
    public void post() {
        if (this.slot != player.heldSlot) {
            player.heldSlot = this.slot;
            player.isEating = false;
            player.isPullingBow = false;
            player.isBlocking = false;
            player.speedData.flagNextTick = false;
        }
    }
}