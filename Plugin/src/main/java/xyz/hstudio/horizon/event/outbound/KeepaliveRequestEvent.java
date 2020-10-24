package xyz.hstudio.horizon.event.outbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.util.Pair;

public class KeepaliveRequestEvent extends OutEvent {

    public final int id;

    public KeepaliveRequestEvent(HPlayer p, int id) {
        super(p);
        this.id = id;
    }

    @Override
    public void post() {
        p.pings.addLast(new Pair<>(id, System.currentTimeMillis()));
        super.post();
    }
}
