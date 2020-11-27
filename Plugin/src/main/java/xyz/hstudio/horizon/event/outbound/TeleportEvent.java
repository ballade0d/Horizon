package xyz.hstudio.horizon.event.outbound;

import net.minecraft.server.v1_8_R3.PacketPlayOutPosition;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;

public class TeleportEvent extends OutEvent<PacketPlayOutPosition> {

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
        Location loc = new Location(p.getWorld(), x, y, z, yaw % 360f, pitch % 360f);
        p.teleports.add(new Pair<>(loc, System.currentTimeMillis()));
        if (p.teleports.size() > 200) {
            p.teleports.remove(0);
        }
    }
}