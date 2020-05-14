package xyz.hstudio.horizon.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.util.enums.Hand;

public class SwingEvent extends Event {

    public final Hand hand;

    public SwingEvent(final HoriPlayer player, final Hand hand) {
        super(player);
        this.hand = hand;
    }
}