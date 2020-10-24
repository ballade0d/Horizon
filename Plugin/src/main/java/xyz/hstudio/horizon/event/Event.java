package xyz.hstudio.horizon.event;

import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;

@RequiredArgsConstructor
public abstract class Event {

    protected static final Horizon inst = Horizon.getPlugin(Horizon.class);

    protected final HPlayer p;

    public boolean pre() {
        return true;
    }

    public void post() {
    }
}
