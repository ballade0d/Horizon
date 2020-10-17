package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import xyz.hstudio.horizon.HPlayer;

@RequiredArgsConstructor
public abstract class InEvent {

    protected final HPlayer p;
    @Getter
    @Setter
    private boolean cancelled = false;

    public boolean pre() {
        return true;
    }

    public void post() {
    }
}