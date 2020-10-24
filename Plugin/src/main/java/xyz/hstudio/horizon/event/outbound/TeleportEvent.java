package xyz.hstudio.horizon.event.outbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.util.Location;

public class TeleportEvent extends OutEvent {

    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public TeleportEvent(HPlayer p, double x, double y, double z, float yaw, float pitch) {
        super(p);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void post() {
        p.status.isTeleporting = true;
        p.addTeleport(new Location(p.getWorld(), x, y, z, yaw % 360F, pitch % 360F));
        inst.getAsync().clearHistory(p.base);
    }
}