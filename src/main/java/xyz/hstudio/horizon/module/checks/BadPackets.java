package xyz.hstudio.horizon.module.checks;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.EntityActionEvent;
import xyz.hstudio.horizon.event.inbound.HeldItemEvent;
import xyz.hstudio.horizon.module.CheckBase;

@LoadFrom("checks/bad_packets.yml")
public class BadPackets extends CheckBase {

    @LoadPath("enable")
    private static boolean ENABLE;

    public BadPackets(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof HeldItemEvent) {
            held((HeldItemEvent) event);
        } else if (event instanceof EntityActionEvent) {
            sneak((EntityActionEvent) event);
        }
    }

    private void held(HeldItemEvent e) {
        if (p.inventory.heldSlot == e.heldItemSlot) {
            punish(e, "BadPackets (Y2xI0)", 1, Detection.BAD_PACKETS, null);
        }
    }

    private void sneak(EntityActionEvent e) {
        if (e.type == EntityActionEvent.ActionType.START_SNEAKING) {
            if (p.status.isSneaking && p.nms.isSneaking()) {
                punish(e, "BadPackets (YwjXR)", 1, Detection.BAD_PACKETS, null);
            }
        } else if (e.type == EntityActionEvent.ActionType.STOP_SNEAKING) {
            if (!p.status.isSneaking && !p.nms.isSneaking()) {
                punish(e, "BadPackets (YwjXR)", 1, Detection.BAD_PACKETS, null);
            }
        }
    }
}