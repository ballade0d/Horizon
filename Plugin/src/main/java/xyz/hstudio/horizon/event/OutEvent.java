package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.Setter;
import xyz.hstudio.horizon.HPlayer;

public abstract class OutEvent extends Event {

    @Getter
    @Setter
    private boolean cancelled = false;

    public OutEvent(HPlayer p) {
        super(p);
    }
}