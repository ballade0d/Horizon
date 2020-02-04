package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class KeepAliveRespondEvent extends Event {

    public final long id;

    public KeepAliveRespondEvent(final HoriPlayer player, final long id) {
        super(player);
        this.id = id;
    }
}