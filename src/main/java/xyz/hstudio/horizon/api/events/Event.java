package xyz.hstudio.horizon.api.events;

import lombok.Getter;
import lombok.Setter;
import xyz.hstudio.horizon.data.HoriPlayer;

public abstract class Event {

    public final HoriPlayer player;
    @Getter
    @Setter
    private boolean cancelled;

    public Event(final HoriPlayer player) {
        this.player = player;
        this.cancelled = false;
    }

    // This is the default pre method to reduce jar size.
    public boolean pre() {
        return true;
    }

    // This is the default post method to reduce jar size.
    public void post() {
    }
}