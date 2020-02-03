package xyz.hstudio.horizon.network.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class BadMoveEvent extends Event {

    public BadMoveEvent(final HoriPlayer player, final WrappedPacket packet) {
        super(player, packet);
    }

    @Override
    public boolean pre() {
        return false;
    }
}