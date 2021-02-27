package xyz.hstudio.horizon.module.checks;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.MathUtils;

@LoadFrom("checks/aim_assist.yml")
public class AimAssist extends CheckBase {

    @LoadPath("enable")
    private static boolean ENABLE;

    private float lastDeltaPitch;

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.teleport || Math.abs(e.to.pitch) == 90f || !e.hasLook) {
                return;
            }
            float deltaPitch = Math.abs(e.to.pitch - e.from.pitch);
            gcd(e, deltaPitch);

            lastDeltaPitch = deltaPitch;
        }
    }

    private void gcd(MoveEvent e, float deltaPitch) {
        if (deltaPitch == 0) {
            return;
        }

        float gcdPitch = MathUtils.gcd(deltaPitch, lastDeltaPitch);
        if (gcdPitch < 0.00001) {
            // System.out.println("Invalid GCD");
        }
    }
}