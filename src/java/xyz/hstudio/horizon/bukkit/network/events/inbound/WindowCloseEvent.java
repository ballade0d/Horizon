package xyz.hstudio.horizon.bukkit.network.events.inbound;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class WindowCloseEvent extends Event {

    public WindowCloseEvent(final HoriPlayer player, final WrappedPacket packet) {
        super(player, packet);
    }
}