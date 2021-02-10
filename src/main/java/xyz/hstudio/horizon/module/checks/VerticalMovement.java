package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
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
@LoadFrom("checks/vertical_movement.yml")
public class VerticalMovement extends CheckBase {

    @LoadInfo("enable")
    private static boolean ENABLE;
    @LoadInfo("precision")
    private static double PRECISION;

    private float estimatedYVelocity;

    public VerticalMovement(HPlayer p) {
        super(p, 1, 10, 10);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            if (p.nms.vehicle != null) {
                return;
            }
            move((MoveEvent) event);
            step((MoveEvent) event);
        }
    }

    private void move(MoveEvent e) {
        float deltaY = (float) e.velocity.y;
        float estimatedYVelocity = (this.estimatedYVelocity + -0.08F) * 0.98F;
        boolean skip = false;
        if (e.teleport || p.status.teleport || e.step) {
            estimatedYVelocity = 0.0F;
            skip = true;
        } else if (e.jump || e.knockBack || e.onSlime) {
            estimatedYVelocity = deltaY;
            skip = true;
        } else if (e.onGround && !this.p.physics.onGround) {
            estimatedYVelocity = 0F;
            skip = true;
        } else if (Math.abs(estimatedYVelocity) < 0.005 && deltaY <= 0 && p.physics.velocity.y > 0) {
            // Jump error
            estimatedYVelocity = deltaY == 0 ? 0 : -(Math.min(Math.abs(deltaY - -0.0784F), Math.abs(deltaY - -0.0754F)) - deltaY);
            skip = true;
        }

        // Jump under block false positive
        boolean hitHead = e.touchedFaces.contains(Direction.UP);
        boolean hasHitHead = p.physics.touchedFaces.contains(Direction.UP);
        if (hitHead && !hasHitHead) {
            deltaY = estimatedYVelocity = 0;
        }

        // Tower false positive
        if (p.physics.onGround && !e.onGround && p.physics.velocity.y == 0 && (Math.abs(deltaY - 0.4044449) < 0.001 || Math.abs(deltaY - 0.3955759) < 0.001)) {
            deltaY = estimatedYVelocity = 0.42F;
        }

        if (!skip) {
            if (e.onGround) {
                estimatedYVelocity = 0F;
            }

            float error = Math.abs(deltaY - estimatedYVelocity);
            if (error > PRECISION) {
                punish(e, "VerticalMovement (5GjoR)", Math.abs(error) * 5, Detection.VERTICAL_MOVEMENT,
                        "d:" + deltaY + ", e:" + estimatedYVelocity + ", e:" + error);
            }
        }
        this.estimatedYVelocity = estimatedYVelocity;
    }

    private void step(MoveEvent e) {
        if (e.step || e.teleport || e.knockBack || e.onSlime) {
            return;
        }
        double deltaY = e.velocity.y;

        if ((deltaY > 0.6 || deltaY < -0.0784) && e.onGround && p.physics.onGround) {
            punish(e, "VerticalMovement (c4slM)", 4, Detection.VERTICAL_MOVEMENT,
                    "d:" + deltaY);
        } else if (e.onGroundReally && Math.abs(p.physics.oldVelocity.y - 0.333) < 0.01 &&
                Math.abs(p.physics.velocity.y - 0.248) < 0.01 && deltaY <= 0) {
            punish(e, "VerticalMovement (14b0l)", 4, Detection.VERTICAL_MOVEMENT,
                    "d:" + deltaY);
        }
    }
}