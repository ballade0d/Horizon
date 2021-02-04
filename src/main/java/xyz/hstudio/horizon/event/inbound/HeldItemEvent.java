package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInHeldItemSlot;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;

public class HeldItemEvent extends Event<PacketPlayInHeldItemSlot> {

    public final int heldItemSlot;

    public HeldItemEvent(HPlayer p, int heldItemSlot) {
        super(p);
        this.heldItemSlot = heldItemSlot;
    }

    @Override
    public void post() {
        p.inventory.heldSlot = heldItemSlot;
    }
}