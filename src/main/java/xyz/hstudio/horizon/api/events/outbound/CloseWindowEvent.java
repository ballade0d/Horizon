package xyz.hstudio.horizon.api.events.outbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class CloseWindowEvent extends Event {

    // It's unnecessary to get all values from PacketPlayOutCloseWindow

    public CloseWindowEvent(final HoriPlayer player) {
        super(player);
    }
}