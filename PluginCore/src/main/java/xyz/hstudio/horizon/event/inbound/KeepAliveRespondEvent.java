package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class KeepAliveRespondEvent extends Event {

    public final long id;

    public KeepAliveRespondEvent(final HoriPlayer player, final long id) {
        super(player);
        this.id = id;
    }
}