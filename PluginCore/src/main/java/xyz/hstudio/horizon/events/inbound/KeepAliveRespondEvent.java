package xyz.hstudio.horizon.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public class KeepAliveRespondEvent extends Event {

    public final long id;

    public KeepAliveRespondEvent(final HoriPlayer player, final long id) {
        super(player);
        this.id = id;
    }
}