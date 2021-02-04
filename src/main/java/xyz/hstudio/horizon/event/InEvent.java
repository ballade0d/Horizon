package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListenerPlayIn;
import xyz.hstudio.horizon.HPlayer;

public abstract class InEvent<T extends Packet<PacketListenerPlayIn>> extends Event<T> {

    @Getter
    @Setter
    private boolean cancelled = false;

    public InEvent(HPlayer p) {
        super(p);
    }
}