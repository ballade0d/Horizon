package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.HeldItemEvent;

public class BadPackets extends CheckBase {

    public BadPackets(HPlayer p) {
        super(p, 0, 0, -1);
    }

    @Override
    public void received(InEvent<?> event) {
        if (event instanceof HeldItemEvent) {
            held((HeldItemEvent) event);
        }
    }

    private void held(HeldItemEvent e) {
        if (p.inventory.heldSlot == e.heldItemSlot) {
            System.out.println("Duplicate held item packets");
        }
    }
}