package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.Setter;
import xyz.hstudio.horizon.HPlayer;

public abstract class InEvent extends Event {

    @Getter
    @Setter
    private boolean cancelled = false;

    public InEvent(HPlayer p) {
        super(p);
    }
}