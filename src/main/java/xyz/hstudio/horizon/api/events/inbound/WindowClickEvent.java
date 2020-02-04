package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class WindowClickEvent extends Event {

    public final int windowID;
    public final int slot;
    public final int button;

    public WindowClickEvent(final HoriPlayer player, final int windowID, final int slot, final int button) {
        super(player);
        this.windowID = windowID;
        this.slot = slot;
        this.button = button;
    }
}