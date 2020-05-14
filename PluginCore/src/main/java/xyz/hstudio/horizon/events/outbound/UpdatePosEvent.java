package xyz.hstudio.horizon.events.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public class UpdatePosEvent extends Event {

    public UpdatePosEvent(final HoriPlayer player, final Object rawPacket) {
        super(player, rawPacket);
    }
}