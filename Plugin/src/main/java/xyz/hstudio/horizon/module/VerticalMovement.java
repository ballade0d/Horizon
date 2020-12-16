package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;

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

    private float estimatedYVelocity;

    public VerticalMovement(HPlayer p) {
        super(p, 1, 10, 10);
    }

    public void received(InEvent<?> event) {
        if (event instanceof MoveEvent) {
            move((MoveEvent) event);
        }
    }

    private void move(MoveEvent e) {
        float deltaY = (float) e.velocity.y;
        float estimatedYVelocity = (this.estimatedYVelocity + -0.08F) * 0.98F;
        boolean skip = false;
        if (e.step) {
            estimatedYVelocity = 0.0F;
            skip = true;
        } else if (e.jump || e.knockBack) {
            estimatedYVelocity = deltaY;
            skip = true;
        } else if (e.onGround && !this.p.physics.onGround) {
            estimatedYVelocity = 0F;
            skip = true;
        } else if (Math.abs(estimatedYVelocity) < 0.005 && deltaY <= 0 && p.physics.velocity.y > 0) {
            estimatedYVelocity = deltaY == 0 ? 0 : -(Math.min(Math.abs(deltaY - -0.0784F), Math.abs(deltaY - -0.0754F)) - deltaY);
            skip = true;
        }

        if (!skip) {
            if (e.onGround) {
                estimatedYVelocity = 0F;
            }

            float error = Math.abs(deltaY - estimatedYVelocity);
            if (error > 0.001) {
                System.out.println("Invalid motion! Delta:" + deltaY + ", Error:" + error + ", Estimated:" + estimatedYVelocity);
            }
        }
        this.estimatedYVelocity = estimatedYVelocity;
    }
}