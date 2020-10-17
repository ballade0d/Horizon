package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class WindowCloseEvent extends InEvent {

    public final int id;

    public WindowCloseEvent(HPlayer p, int id) {
        super(p);
        this.id = id;
    }
}