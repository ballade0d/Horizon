package xyz.hstudio.horizon.event.outbound;

import me.cgoo.api.util.IntObjPair;
import net.minecraft.server.v1_8_R3.PacketPlayOutKeepAlive;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;

public class KeepaliveRequestEvent extends Event<PacketPlayOutKeepAlive> {

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
        p.pings.offer(new IntObjPair<>(id, System.currentTimeMillis()));
    }
}
