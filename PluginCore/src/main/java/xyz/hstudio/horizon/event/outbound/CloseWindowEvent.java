package xyz.hstudio.horizon.event.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class CloseWindowEvent extends Event {

    // It's unnecessary to get all values from PacketPlayOutCloseWindow

    public CloseWindowEvent(final HoriPlayer player) {
        super(player);
    }
}