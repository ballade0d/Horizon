package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.MathUtils;

public class AimAssist extends CheckBase {

    private double lastSensitivity = 0;
    private float lastDeltaPitch = 0f;
    private int buffer = 0;

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    private static double modulusRotation(double sensitivity, double pitch) {
        return pitch % Math.pow(sensitivity * 0.6000000238418579 + 0.20000000298023224, 3) * 1.2;
    }

    private static double gcdToSensitive(float gcd) {
        return (Math.cbrt(gcd / 0.15 / 8.0) - 0.2) / 0.6;
    }

    private static double sensToPercent(double sensitivity) {
        return sensitivity / 0.5 * 100;
    }

    @Override
    public void received(InEvent<?> event) {
        if (event instanceof MoveEvent) {
            mousePrediction((MoveEvent) event);
            gcd((MoveEvent) event);
        }
    }

    /**
     * This false flags with large pitch movements but not with OptiFine zoom :)
     */
    private void mousePrediction(MoveEvent e) {

        float deltaPitch = Math.abs(e.to.pitch - e.from.pitch);
        float gcd = MathUtils.gcd(deltaPitch, lastDeltaPitch);
        double sensitivity = sensToPercent(gcdToSensitive(gcd));

        /*
        // Check if sensitivity is invalid
        if (sensitivity > 200 || sensitivity < 0) {
            System.out.println("Invalid sensitivity");
        }
        */

        // Try to predict rotation using sensitivity.
        // TODO improve this

        double f1 = ((lastSensitivity * 0.6f + 0.2f) * lastDeltaPitch);
        double mouseDelta = (e.to.pitch - e.from.pitch) / f1 * 0.15;
        double expectedPitch = e.from.pitch + mouseDelta;

        // Check if difference between actual and expected is too big.
        // Ideally the buffer isn't needed.
        if (Math.abs(e.to.pitch - expectedPitch) >= 3 && buffer++ >= 3) {
            buffer = 0;
            // System.out.println("Unexpected rotation! " + Math.abs(e.to.pitch - expectedPitch));
        }

        /*
        System.out.println("expected: " + expectedPitch);
        System.out.println("actual: " + e.to.pitch);
        */

        lastSensitivity = sensitivity;
    }

    private void gcd(MoveEvent e) {
        if (e.teleport || Math.abs(e.to.pitch) >= 89f) {
            return;
        }

        float pitch = e.to.pitch;
        float deltaPitch = Math.abs(pitch - e.from.pitch);

        if (deltaPitch == 0) {
            return;
        }

        float gcd = MathUtils.gcd(deltaPitch, lastDeltaPitch);
        if (gcd < 0.00001) {
            System.out.println("Invalid GCD");
        }

        double sensitivity = sensToPercent(gcdToSensitive(gcd));
        double o = modulusRotation(sensitivity, pitch);
        int len = String.valueOf(o).length();
        // Add a lot of vl?
        if (o == 0.0) {
            System.out.println("Invalid A");
        }
        if (sensitivity > 99 && o > 0 && len > 0 && len < 4) {
            System.out.println("Invalid B l:" + len + ", o:" + o);
        }

        lastDeltaPitch = deltaPitch;
    }
}