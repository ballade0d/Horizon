package xyz.hstudio.horizon.network.events.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class VelocityEvent extends Event {

    public final double x;
    public final double y;
    public final double z;

    public VelocityEvent(final HoriPlayer player, final double x, final double y, final double z, final WrappedPacket packet) {
        super(player, packet);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        player.velocities.add(new Pair<>(new Vector3D(x, y, z), System.currentTimeMillis()));
    }
}