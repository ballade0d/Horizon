package xyz.hstudio.horizon.event.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.util.wrap.Location;

public class PlayerTeleportEvent extends Event {

    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public PlayerTeleportEvent(final HoriPlayer player, final double x, final double y, final double z, final float yaw, final float pitch) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void post() {
        player.isTeleporting = true;
        player.addTeleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
    }
}