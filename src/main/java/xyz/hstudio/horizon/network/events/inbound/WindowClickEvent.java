package xyz.hstudio.horizon.network.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class WindowClickEvent extends Event {

    public final int windowID;
    public final int slot;
    public final int button;

    public WindowClickEvent(final HoriPlayer player, final int windowID, final int slot, final int button, final WrappedPacket packet) {
        super(player, packet);
        this.windowID = windowID;
        this.slot = slot;
        this.button = button;
    }
}