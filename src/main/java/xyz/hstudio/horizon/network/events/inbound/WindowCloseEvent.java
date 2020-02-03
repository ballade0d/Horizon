package xyz.hstudio.horizon.network.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class WindowCloseEvent extends Event {

    public WindowCloseEvent(final HoriPlayer player, final WrappedPacket packet) {
        super(player, packet);
    }
}