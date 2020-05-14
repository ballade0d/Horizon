package xyz.hstudio.horizon.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public class InteractCSEntityEvent extends Event {

    public final InteractEntityEvent.InteractType action;
    public final int id;

    public InteractCSEntityEvent(final HoriPlayer player, final InteractEntityEvent.InteractType action, final int id) {
        super(player);
        this.action = action;
        this.id = id;
    }
}