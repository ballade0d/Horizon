package xyz.hstudio.horizon.api.events.outbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class OpenWindowEvent extends Event {

    // It's unnecessary to get all values from PacketPlayOutOpenWindow

    public OpenWindowEvent(final HoriPlayer player) {
        super(player);
    }

    @Override
    public boolean pre() {
        player.isEating = false;
        player.isPullingBow = false;
        player.isBlocking = false;
        return true;
    }
}