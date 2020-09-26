package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;

import static xyz.hstudio.horizon.util.Physics.AIR_RESISTANCE_VERTICAL;

/**
 * Vertical movement check.
 * <p>
 * a = (vt - v0) / t -> vt = v0 + at
 * <p>
 * vt is the velocity of the time t
 * v0 is the velocity of the beginning
 * a is the gravitational acceleration (g = -0.08 in Minecraft)
 * t is the time, measured in ticks
 * <p>
 * we also need to multiply it with vertical air resistance (k = 0.98 in Minecraft)
 *
 * @see xyz.hstudio.horizon.util.Physics
 * so expectedYVelocity = (prevYVelocity + -0.08 * 1) * 0.98
 */
public class VerticalMovement extends CheckBase {

    public VerticalMovement(HPlayer p) {
        super(p, 1, 10, 10);
    }

    private double airYVelFunc(double initVelocityY, long ticks) {
        return (3.92 + initVelocityY) * Math.pow(AIR_RESISTANCE_VERTICAL, ticks) - 3.92;
    }
}