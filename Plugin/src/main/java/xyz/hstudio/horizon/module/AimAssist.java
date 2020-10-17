package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.MathUtils;

public class AimAssist extends CheckBase {

    private float lastDeltaPitch;

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    @Override
    public void received(InEvent event) {

    }

    private void gcd(MoveEvent e) {
        if (e.teleport) {
            return;
        }
        float deltaPitch = e.to.pitch - p.physics.position.pitch;
        if (deltaPitch == 0) {
            return;
        }

        float gcd = MathUtils.gcd(deltaPitch, lastDeltaPitch);

        if (gcd < 0.00001) {
            System.out.println("Invalid Rotation");
        }

        this.lastDeltaPitch = deltaPitch;
    }
}
