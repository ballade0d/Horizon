package xyz.hstudio.horizon.module

import xyz.hstudio.horizon.HPlayer
import xyz.hstudio.horizon.event.InEvent
import xyz.hstudio.horizon.event.inbound.MoveEvent
import xyz.hstudio.horizon.util.Physics.AIR_RESISTANCE_VERTICAL
import xyz.hstudio.horizon.util.Physics.GRAVITATIONAL_ACCELERATION
import kotlin.math.abs

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
class VerticalMovement(p: HPlayer) : CheckBase(p, 1, 10, 10) {

    private var estimatedYVelocity = 0f

    override fun received(event: InEvent?) {
        if (event is MoveEvent) {
            move(event)
        }
    }

    private fun move(e: MoveEvent) {
        val deltaY = e.velocity.y.toFloat()
        var estimatedYVelocity = (estimatedYVelocity + GRAVITATIONAL_ACCELERATION) * AIR_RESISTANCE_VERTICAL
        var skip = false
        when {
            e.step -> {
                estimatedYVelocity = 0f
                skip = true
            }
            e.jump -> {
                estimatedYVelocity = deltaY
                skip = true
            }
            e.onGround && !p.physics.onGround -> {
                estimatedYVelocity = deltaY
                skip = true
            }
        }
        if (!skip) {
            if (e.onGround) {
                estimatedYVelocity = 0f
            } else {
                // Fix jump bug when standing still
                // Wtf MoJang???
                if (abs(estimatedYVelocity) < 0.005 && deltaY < 0 && p.physics.velocity.y > 0 && e.to.distance2dSquared(p.physics.position) < 1E-6) {
                    estimatedYVelocity = deltaY
                }
                /*
                // Fix 1.8 jump bug
                if (abs(estimatedYVelocity) < 0.003 && estimatedYVelocity != 0f && deltaY <= 0) {
                    estimatedYVelocity = deltaY
                }
                // Fix 1.9 jump bug
                if (deltaY < 0 && p.physics.prevVelocity.y >= 0 && e.to.distance2dSquared(p.physics.position) < 1E-6) {
                    estimatedYVelocity = deltaY
                }
                */
            }

            // Keep four decimal places
            val error = ((deltaY - estimatedYVelocity) * 10000).toInt() / 10000f
            if (error > 0.001) {
                println("Error:$error")
            }
        }

        this.estimatedYVelocity = estimatedYVelocity
    }
}