package xyz.hstudio.horizon.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public class WindowCloseEvent extends Event {

    public WindowCloseEvent(final HoriPlayer player) {
        super(player);
    }
}