package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

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