package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.EntityActionEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.module.CheckBase;

public class KillAura extends CheckBase {

    private int startSprintTick;
    private int kbTick;

    public KillAura(HPlayer p) {
        super(p);
    }

    @Override
    public void received(InEvent<?> event) {
        superKb(event);
    }

    private void superKb(InEvent<?> event) {
        if (event instanceof EntityActionEvent) {
            EntityActionEvent e = (EntityActionEvent) event;
            switch (e.type) {
                case START_SPRINTING:
                    startSprintTick = p.currTick;
                    break;
                case STOP_SPRINTING:
                    kbTick = startSprintTick;
                    break;
                default:
                    break;
            }
        } else if (event instanceof EntityInteractEvent) {
            EntityInteractEvent e = (EntityInteractEvent) event;
            if (e.type != EntityInteractEvent.InteractType.ATTACK) {
                return;
            }
            if (p.currTick == kbTick) {
                punish(e, "KillAura (JtXSV)", 3, Detection.KILL_AURA, null);
            }
        }
    }
}