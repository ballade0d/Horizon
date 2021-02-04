package xyz.hstudio.horizon.module.checks;

import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.MathUtils;

public class AimAssist extends CheckBase {

    /*
     * Gcd
     */
    private float lastDeltaYaw = 0f;
    private float lastDeltaPitch = 0f;

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    private static double modulusRotation(double sensitivity, float pitch) {
        return pitch % Math.pow(sensitivity * 0.6000000238418579 + 0.20000000298023224, 3) * 1.2;
    }

    private static double gcdToSensitive(float gcd) {
        return (Math.cbrt(gcd / 0.15 / 8.0) - 0.2) / 0.6;
    }

    private static int sensToPercent(double sensitivity) {
        return NumberConversions.round(sensitivity / 0.5) * 100;
    }

    @Override
    public void run(Event<?> event) {
        if (event instanceof MoveEvent) {
            //mousePrediction((MoveEvent) event);
            gcd((MoveEvent) event);
        }
    }

    /*
    private double lastSensitivity = 0;
    private int gcdBuffer = 0;

    // This false flags with large pitch movements but not with OptiFine zoom :)
    private void mousePrediction(MoveEvent e) {

        float deltaPitch = Math.abs(e.to.pitch - e.from.pitch);
        float gcd = MathUtils.gcd(deltaPitch, lastDeltaPitch);
        double sensitivity = sensToPercent(gcdToSensitive(gcd));


        // Check if sensitivity is invalid
        // if (sensitivity > 200 || sensitivity < 0) {
        //     System.out.println("Invalid sensitivity");
        // }

        // Try to predict rotation using sensitivity.
        // TODO improve this

        double f1 = ((lastSensitivity * 0.6f + 0.2f) * lastDeltaPitch);
        double mouseDelta = (e.to.pitch - e.from.pitch) / f1 * 0.15;
        double expectedPitch = e.from.pitch + mouseDelta;

        // Check if difference between actual and expected is too big.
        // Ideally the buffer isn't needed.
        if (Math.abs(e.to.pitch - expectedPitch) >= 3 && gcdBuffer++ >= 3) {
            gcdBuffer = 0;
            // System.out.println("Unexpected rotation! " + Math.abs(e.to.pitch - expectedPitch));
        }


        // System.out.println("expected: " + expectedPitch);
        // System.out.println("actual: " + e.to.pitch);

        lastSensitivity = sensitivity;
    }
    */

    private void gcd(MoveEvent e) {
        if (e.teleport || Math.abs(e.to.pitch) >= 89f) {
            return;
        }

        float deltaYaw = Math.abs(e.to.yaw - e.from.yaw);
        float pitch = e.to.pitch;
        float deltaPitch = Math.abs(pitch - e.from.pitch);

        if (deltaPitch == 0 || Math.abs(pitch) == 90) {
            return;
        }

        float gcdPitch = MathUtils.gcd(deltaPitch, lastDeltaPitch);
        if (gcdPitch < 0.00001) {
            // System.out.println("Invalid GCD");
            new Object();
        }

        float gcdYaw = MathUtils.gcd(deltaYaw, lastDeltaYaw);
        int sensitivity = sensToPercent(gcdToSensitive(gcdYaw));
        double mod = modulusRotation(sensitivity, pitch);
        if (mod == 0) {
            punish(e, "AimAssist (0dE4u)", 10, Detection.AIM_ASSIST,
                    null);
        }
        int len = String.valueOf(mod).length();
        if (mod > 0 && len > 0 && len < 8) {
            punish(e, "AimAssist (DGGub)", 3, Detection.AIM_ASSIST,
                    "mod:" + mod + ", l:" + len);
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }
}