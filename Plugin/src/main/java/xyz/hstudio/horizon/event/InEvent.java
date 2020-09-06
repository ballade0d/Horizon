package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.Setter;

public abstract class InEvent {

    @Getter
    @Setter
    private boolean cancelled = false;

    public boolean pre() {
        return true;
    }

    public void post() {
    }
}