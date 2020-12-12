package xyz.hstudio.horizon.event.outbound;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityVelocity;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.OutEvent;

public class VelocityEvent extends OutEvent<PacketPlayOutEntityVelocity> {

    public final float x;
    public final float y;
    public final float z;

    public VelocityEvent(HPlayer p, float x, float y, float z) {
        super(p);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void post() {
        p.sendSimulatedAction(() -> {
            p.velocity.x = this.x;
            p.velocity.y = this.y;
            p.velocity.z = this.z;
            p.velocity.firstTick = true;
            p.velocity.time = System.currentTimeMillis();
        });
    }
}