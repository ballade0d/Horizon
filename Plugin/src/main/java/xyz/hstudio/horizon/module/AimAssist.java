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
        if (e.teleport || e.to.pitch >= 90) {
            return;
        }
        float deltaPitch = Math.abs(e.to.pitch - p.physics.position.pitch);
        if (deltaPitch == 0) {
            return;
        }

        float gcd = MathUtils.gcd(deltaPitch, lastDeltaPitch);

        if (gcd < 0.00001) {
            System.out.println("Invalid Rotation");
        }

        this.lastDeltaPitch = deltaPitch;
    }

    private double getSensitivity(double gcd) {
        return (5D / 3D * Math.cbrt(5D / 6D * gcd) - 1D / 3D) * 200;
    }
}