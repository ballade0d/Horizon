package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.EntityActionEvent;
import xyz.hstudio.horizon.event.inbound.HeldItemEvent;
import xyz.hstudio.horizon.module.CheckBase;

public class BadPackets extends CheckBase {

    public BadPackets(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        if (event instanceof HeldItemEvent) {
            held((HeldItemEvent) event);
        } else if (event instanceof EntityActionEvent) {
            sneak((EntityActionEvent) event);
        }
    }

    private void held(HeldItemEvent e) {
        if (p.inventory.heldSlot == e.heldItemSlot) {
            punish(e, "BadPackets (Y2xI0)", 10, Detection.BAD_PACKETS, null);
        }
    }

    private void sneak(EntityActionEvent e) {
        if (e.type == EntityActionEvent.ActionType.START_SNEAKING) {
            if (p.status.isSneaking && p.bukkit.isSneaking()) {
                punish(e, "BadPackets (YwjXR)", 10, Detection.BAD_PACKETS, null);
            }
        } else if (e.type == EntityActionEvent.ActionType.STOP_SNEAKING) {
            if (!p.status.isSneaking && !p.bukkit.isSneaking()) {
                punish(e, "BadPackets (YzQap)", 10, Detection.BAD_PACKETS, null);
            }
        }
    }
}