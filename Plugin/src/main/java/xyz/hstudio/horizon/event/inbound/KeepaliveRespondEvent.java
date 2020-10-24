package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Pair;

public class KeepaliveRespondEvent extends InEvent {

    public final int id;

    public KeepaliveRespondEvent(HPlayer p, int id) {
        super(p);
        this.id = id;
    }

    @Override
    public boolean pre() {
        Pair<Integer, Long> first = p.pings.pollFirst();
        if (first == null) {
            // Kick
            return false;
        }
        if (first.getKey() != id) {
            // Kick
            return false;
        }
        int ping = (int) (System.currentTimeMillis() - first.getValue());
        p.status.ping = (p.status.ping * 3 + ping) / 4;
        return super.pre();
    }
}