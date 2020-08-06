package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.util.enums.Hand;

public class SwingEvent extends Event {

    public final Hand hand;

    public SwingEvent(final HoriPlayer player, final Hand hand) {
        super(player);
        this.hand = hand;
    }
}