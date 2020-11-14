package xyz.hstudio.horizon.event.outbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vector3D;

public class VelocityEvent extends OutEvent {

    public final double x;
    public final double y;
    public final double z;

    public VelocityEvent(HPlayer p, float x, float y, float z) {
        super(p);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        p.velocities.add(new Pair<>(new Vector3D(x, y, z), System.currentTimeMillis()));
        if (p.velocities.size() > 20) p.velocities.remove(0);
    }
}