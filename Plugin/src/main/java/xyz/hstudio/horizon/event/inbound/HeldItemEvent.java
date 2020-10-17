package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class HeldItemEvent extends InEvent {

    public final int heldItemSlot;

    public HeldItemEvent(HPlayer p, int heldItemSlot) {
        super(p);
        this.heldItemSlot = heldItemSlot;
    }
}