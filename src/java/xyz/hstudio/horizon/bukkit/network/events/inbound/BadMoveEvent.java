package xyz.hstudio.horizon.bukkit.network.events.inbound;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class BadMoveEvent extends Event {

    public BadMoveEvent(final HoriPlayer player, final WrappedPacket packet) {
        super(player, packet);
    }

    @Override
    public boolean pre() {
        return false;
    }

    @Override
    public void post() {
    }
}