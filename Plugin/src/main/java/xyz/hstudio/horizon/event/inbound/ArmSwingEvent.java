package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class ArmSwingEvent extends InEvent<PacketPlayInArmAnimation> {

    public ArmSwingEvent(HPlayer p) {
        super(p);
    }
}