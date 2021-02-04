package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListenerPlayOut;
import xyz.hstudio.horizon.HPlayer;

public abstract class OutEvent<T extends Packet<PacketListenerPlayOut>> extends Event<T> {

    @Getter
    @Setter
    private boolean cancelled = false;

    public OutEvent(HPlayer p) {
        super(p);
    }
}