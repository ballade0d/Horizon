package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.ArmSwingEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.module.CheckBase;

@LoadFrom("checks/no_swing.yml")
public class NoSwing extends CheckBase {

    @LoadInfo("enable")
    private static boolean ENABLE;

    private boolean swingExpected;

    public NoSwing(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        hit(event);
    }

    private void hit(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof ArmSwingEvent) {
            swingExpected = false;
        } else if (event instanceof EntityInteractEvent) {
            EntityInteractEvent e = (EntityInteractEvent) event;
            if (e.type != EntityInteractEvent.InteractType.ATTACK) {
                return;
            }
            if (swingExpected) {
                punish(e, "g9Fkq", 1, Detection.NO_SWING, null);
            }
            swingExpected = true;
        }
    }
}