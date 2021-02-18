package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInAbilities;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;

public class CAbilitiesEvent extends Event<PacketPlayInAbilities> {

    public final boolean invulnerable;
    public final boolean flying;
    public final boolean canFly;
    public final boolean inCreative;
    public final float flyingSpeed;
    public final float walkingSpeed;

    public CAbilitiesEvent(HPlayer p, boolean invulnerable, boolean flying, boolean canFly, boolean inCreative, float flyingSpeed, float walkingSpeed) {
        super(p);
        this.invulnerable = invulnerable;
        this.flying = flying;
        this.canFly = canFly;
        this.inCreative = inCreative;
        this.flyingSpeed = flyingSpeed;
        this.walkingSpeed = walkingSpeed;
    }

    @Override
    public void post() {
        p.status.isFlying = flying && p.nms.abilities.canFly;
    }
}