package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.enums.Direction;

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
 * <p>
 * so expectedYVelocity = (prevYVelocity + -0.08 * 1) * 0.98
 */
public class VerticalMovement extends CheckBase {

    private float estimatedYVelocity;

    public VerticalMovement(HPlayer p) {
        super(p, 1, 10, 10);
    }

    @Override
    public void received(InEvent<?> event) {
        if (event instanceof MoveEvent) {
            move((MoveEvent) event);
        }
    }

    private void move(MoveEvent e) {
        float deltaY = (float) e.velocity.y;
        float estimatedYVelocity = (this.estimatedYVelocity + -0.08F) * 0.98F;
        boolean skip = false;
        if (e.step || e.teleport) {
            estimatedYVelocity = 0.0F;
            skip = true;
        } else if (e.jump || e.knockBack || e.onSlime) {
            estimatedYVelocity = deltaY;
            skip = true;
        } else if (e.onGround && !this.p.physics.onGround) {
            estimatedYVelocity = 0F;
            skip = true;
        } else if (Math.abs(estimatedYVelocity) < 0.005 && deltaY <= 0 && p.physics.velocity.y > 0) {
            estimatedYVelocity = deltaY == 0 ? 0 : -(Math.min(Math.abs(deltaY - -0.0784F), Math.abs(deltaY - -0.0754F)) - deltaY);
            skip = true;
        }
        // Blocks above
        boolean hitHead = e.touchedFaces.contains(Direction.UP);
        boolean hasHitHead = p.physics.touchedFaces.contains(Direction.UP);
        if (hitHead && !hasHitHead) {
            deltaY = estimatedYVelocity = 0;
        }

        if (!skip) {
            if (e.onGround) {
                estimatedYVelocity = 0F;
            }

            float error = Math.abs(deltaY - estimatedYVelocity);
            if (error > 0.001) {
                System.out.println("Invalid motion! Delta:" + deltaY + ", Estimated:" + estimatedYVelocity + ", Error:" + error);
            }
        }
        this.estimatedYVelocity = estimatedYVelocity;
    }
}