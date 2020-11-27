package xyz.hstudio.horizon.module;

import lombok.val;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.MathUtils;

public class AimAssist extends CheckBase {

    private float lastDeltaPitch = 0f;

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    public void received(InEvent<?> event) {
        if (event instanceof MoveEvent) {
            gcd((MoveEvent) event);
        }
    }

    private void gcd(MoveEvent e) {
        if (e.teleport || Math.abs(e.to.pitch) >= 89f) {
            return;
        }

        float pitch = e.to.pitch;

        float deltaPitch = Math.abs(pitch - p.physics.position.pitch);

        if (deltaPitch == 0) {
            return;
        }

        val gcd = MathUtils.gcd(deltaPitch, lastDeltaPitch);

        if (gcd < 0.00001) {
            System.out.println("Invalid GCD");
        }

        double sensitivity = sensToPercent(gcdToSensitive(gcd));

        double o = modulusRotation(sensitivity, pitch);
        int len = String.valueOf(o).length();
        if (o == 0.0) {
            // Add more vl
            System.out.println("Invalid A");
        }
        if (sensitivity > 99 && o > 0 && len > 0 && len < 8) {
            System.out.println("Invalid B len:$len");
        }

        lastDeltaPitch = deltaPitch;
    }

    private static double modulusRotation(double sensitivity, double pitch) {
        return pitch % Math.pow(sensitivity * 0.6 + 0.2, 3) * 1.2;
    }

    private static double gcdToSensitive(float gcd) {
        return (Math.cbrt(gcd / 0.15 / 8.0) - 0.2) / 0.6;
    }

    private static double sensToPercent(double sensitivity) {
        return sensitivity / 0.5 * 100;
    }
}