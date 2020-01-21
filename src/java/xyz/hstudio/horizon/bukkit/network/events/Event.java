package xyz.hstudio.horizon.bukkit.network.events;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;

public abstract class Event {

    public final HoriPlayer player;
    public final WrappedPacket packet;
    private boolean cancelled;

    public Event(final HoriPlayer player, final WrappedPacket packet) {
        this.player = player;
        this.packet = packet;
        this.cancelled = false;
    }

    // This is the default pre method to reduce jar size.
    public boolean pre() {
        return true;
    }

    // This is the default post method to reduce jar size.
    public void post() {
    }
}