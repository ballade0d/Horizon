package xyz.hstudio.horizon.events.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public class CloseWindowEvent extends Event {

    // It's unnecessary to get all values from PacketPlayOutCloseWindow

    public CloseWindowEvent(final HoriPlayer player) {
        super(player);
    }
}