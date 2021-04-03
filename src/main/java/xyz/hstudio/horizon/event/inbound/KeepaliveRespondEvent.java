package xyz.hstudio.horizon.event.inbound;

import me.cgoo.api.util.IntObjPair;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;

public class KeepaliveRespondEvent extends Event<PacketPlayInKeepAlive> {

    public final int id;

    public KeepaliveRespondEvent(HPlayer p, int id) {
        super(p);
        this.id = id;
    }

    @Override
    public boolean pre() {
        if (p.executeSimulatedAction(id)) {
            // Cancel this packet cuz the packet is used to check
            // if the player has received the packet that has sent by the server
            return false;
        }

        IntObjPair<Long> first = p.pings.poll();
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
        return true;
    }
}