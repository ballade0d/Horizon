package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
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

    private float lastDeltaYaw = 0f;
    private float lastDeltaPitch = 0f;
    private float buffer;

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            gcd((MoveEvent) event);
            pattern((MoveEvent) event);
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
    }

    private void pattern(MoveEvent e) {
        if (!e.hasLook) {
            return;
        }
        float deltaYaw = Math.abs(e.to.yaw - e.from.yaw);
        float deltaPitch = Math.abs(e.to.pitch - e.from.pitch);

        float yawDiff = Math.abs(deltaYaw - lastDeltaYaw);
        float pitchDiff = Math.abs(deltaPitch - lastDeltaPitch);

        float yawChangeDifference = Math.abs(deltaYaw - yawDiff);
        float pitchChangeDifference = Math.abs(deltaPitch - pitchDiff);

        float yawPitchDifference = Math.abs(deltaYaw - deltaPitch);

        if (deltaYaw > 0.05 && deltaPitch > 0.05 &&
                (pitchDiff > 1 || yawDiff > 1) &&
                (pitchChangeDifference > 1 || yawChangeDifference > 1) &&
                yawDiff < 0.009 && yawPitchDifference > 0.001) {
            if (++buffer > 1) {
                punish(e, "AimAssist (9Eg2y)", 3, Detection.AIM_ASSIST, null);
            }
        } else {
            buffer = Math.max(buffer - 0.2f, 0);
        }

        lastDeltaYaw = deltaYaw;
        lastDeltaPitch = deltaPitch;
    }
}