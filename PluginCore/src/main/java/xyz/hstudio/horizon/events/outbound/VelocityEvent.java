package xyz.hstudio.horizon.events.outbound;

import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class VelocityEvent extends Event {

    public static boolean useExplosionPacket;

    public final double x;
    public final double y;
    public final double z;
    public final boolean additive;

    public VelocityEvent(final HoriPlayer player, final double x, final double y, final double z, final boolean additive) {
        super(player);
        this.x = x;
        this.y = y;
        this.z = z;
        this.additive = additive;
    }

    @Override
    public void post() {
        if (additive) {
            return;
        }
        player.velocities.add(new Pair<>(new Vector3D(x, y, z), new Long[]{System.currentTimeMillis(), player.currentTick}));
        if (useExplosionPacket) {
            this.setCancelled(true);
            player.sendPacket(McAccessor.INSTANCE.createExplosionPacket(this.x, this.y, this.z));
        }
    }
}