package xyz.hstudio.horizon.network.events.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class OpenWindowEvent extends Event {

    // It's unnecessary to get all values from PacketPlayOutOpenWindow

    public OpenWindowEvent(final HoriPlayer player, final WrappedPacket packet) {
        super(player, packet);
    }
}