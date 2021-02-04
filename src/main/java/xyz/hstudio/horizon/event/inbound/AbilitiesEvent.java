package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInAbilities;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class AbilitiesEvent extends InEvent<PacketPlayInAbilities> {

    public final boolean invulnerable;
    public final boolean flying;
    public final boolean flyable;
    public final boolean inCreative;
    public final float flyingSpeed;
    public final float walkingSpeed;

    public AbilitiesEvent(HPlayer p, boolean invulnerable, boolean flying, boolean flyable, boolean inCreative, float flyingSpeed, float walkingSpeed) {
        super(p);
        this.invulnerable = invulnerable;
        this.flying = flying;
        this.flyable = flyable;
        this.inCreative = inCreative;
        this.flyingSpeed = flyingSpeed;
        this.walkingSpeed = walkingSpeed;
    }
}