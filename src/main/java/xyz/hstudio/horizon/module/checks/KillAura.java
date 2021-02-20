package xyz.hstudio.horizon.module.checks;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.EntityActionEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.module.CheckBase;

@LoadFrom("checks/kill_aura.yml")
public class KillAura extends CheckBase {

    @LoadPath("enable")
    private static boolean ENABLE;

    private int startSprintTick;
    private int kbTick;

    public KillAura(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        superKb(event);
    }

    private void superKb(Event<?> event) {
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