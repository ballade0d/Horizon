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

    public abstract boolean pre();

    public abstract void post();
}