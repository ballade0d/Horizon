package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.Setter;
import xyz.hstudio.horizon.HPlayer;

public abstract class InEvent {

    @Getter
    @Setter
    private boolean cancelled = false;

    public boolean pre(HPlayer p) {
        return true;
    }

    public void post(HPlayer p) {
    }
}