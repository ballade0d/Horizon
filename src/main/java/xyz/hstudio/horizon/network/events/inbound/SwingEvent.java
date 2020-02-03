package xyz.hstudio.horizon.network.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;
import xyz.hstudio.horizon.util.enums.Hand;

public class SwingEvent extends Event {

    public final Hand hand;

    public SwingEvent(final HoriPlayer player, final Hand hand, final WrappedPacket packet) {
        super(player, packet);
        this.hand = hand;
    }
}