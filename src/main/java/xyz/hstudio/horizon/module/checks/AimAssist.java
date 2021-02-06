package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.MathUtils;

@LoadFrom("checks/aim_assist.yml")
public class AimAssist extends CheckBase {

    @LoadInfo("enable")
    private static boolean ENABLE;

    /*
     * Gcd
     */
    private float lastDeltaPitch = 0f;

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            gcd((MoveEvent) event);
        }
    }

    private void gcd(MoveEvent e) {
        if (e.teleport || Math.abs(e.to.pitch) >= 89f || !e.hasLook) {
            return;
        }

        float pitch = e.to.pitch;
        float deltaPitch = Math.abs(pitch - e.from.pitch);

        if (deltaPitch == 0) {
            return;
        }

        float gcdPitch = MathUtils.gcd(deltaPitch, lastDeltaPitch);
        if (gcdPitch < 0.00001) {
            // System.out.println("Invalid GCD");
        }

        lastDeltaPitch = deltaPitch;
    }
}