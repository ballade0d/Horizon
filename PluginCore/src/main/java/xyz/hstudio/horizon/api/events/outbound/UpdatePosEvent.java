package xyz.hstudio.horizon.api.events.outbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class UpdatePosEvent extends Event {

    public UpdatePosEvent(final HoriPlayer player, final Object rawPacket) {
        super(player, rawPacket);
    }
}