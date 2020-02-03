package xyz.hstudio.horizon.network.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class KeepAliveRespondEvent extends Event {

    public final long id;

    public KeepAliveRespondEvent(final HoriPlayer player, final long id, final WrappedPacket packet) {
        super(player, packet);
        this.id = id;
    }
}