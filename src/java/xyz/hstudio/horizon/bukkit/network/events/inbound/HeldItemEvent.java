package xyz.hstudio.horizon.bukkit.network.events.inbound;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class HeldItemEvent extends Event {

    public final int slot;

    public HeldItemEvent(final HoriPlayer player, final int slot, final WrappedPacket packet) {
        super(player, packet);
        this.slot = slot;
    }

    @Override
    public boolean pre() {
        this.player.heldSlot = this.slot;
        return true;
    }

    @Override
    public void post() {
    }
}