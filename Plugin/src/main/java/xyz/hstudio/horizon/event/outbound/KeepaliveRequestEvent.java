package xyz.hstudio.horizon.event.outbound;

import net.minecraft.server.v1_8_R3.PacketPlayOutKeepAlive;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.util.Pair;

public class KeepaliveRequestEvent extends OutEvent<PacketPlayOutKeepAlive> {

    public final int id;

    public KeepaliveRequestEvent(HPlayer p, int id) {
        super(p);
        this.id = id;
    }

    @Override
    public void post() {
        if (p.simulatedCmds.containsKey(id)) {
            // Don't count this
            return;
        }
        p.pings.offer(new Pair<>(id, System.currentTimeMillis()));
    }
}
