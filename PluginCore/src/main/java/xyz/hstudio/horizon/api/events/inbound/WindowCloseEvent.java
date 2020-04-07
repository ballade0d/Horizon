package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class WindowCloseEvent extends Event {

    public WindowCloseEvent(final HoriPlayer player) {
        super(player);
    }
}