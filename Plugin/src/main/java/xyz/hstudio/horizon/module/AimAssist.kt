package xyz.hstudio.horizon.module

import xyz.hstudio.horizon.HPlayer
import xyz.hstudio.horizon.event.InEvent
import xyz.hstudio.horizon.event.inbound.MoveEvent
import xyz.hstudio.horizon.util.MathUtils
import kotlin.math.abs

class AimAssist(p: HPlayer) : CheckBase(p, 1, 200, 200) {

    private var lastDeltaPitch = 0f

    override fun received(event: InEvent?) {
    }

    private fun gcd(e: MoveEvent) {
        if (e.teleport || e.to.pitch >= 90) {
            return
        }

        val pitch = e.to.pitch

        val deltaPitch = abs(pitch - p.physics.position.pitch)
        if (deltaPitch == 0f) {
            return
        }

        val gcd = MathUtils.gcd(deltaPitch, lastDeltaPitch)
        if (gcd < 0.00001) {
            println("Invalid GCD")
        }

        val sensitivity = gcdToSensitive(gcd)

        val o: Double = this.modulusRotation(sensitivity, pitch.toDouble())

        val l = o.toString().length
        if (abs(pitch) != 90f && ((sensitivity > 99 && o > 0 && l > 0 && l < 8) || o == 0.0)) {
            println("Invalid Rotation")
        }

        lastDeltaPitch = deltaPitch
    }

    private fun gcdToSensitive(gcd: Float): Double {
        return (5.0 / 3.0 * Math.cbrt(5.0 / 6.0 * gcd) - 1.0 / 3.0) * 200
    }

    private fun modulusRotation(sensitivity: Double, pitch: Double): Double {
        val f = (sensitivity * 0.6000000238418579 + 0.20000000298023224).toFloat()
        val f2 = f * f * f * 1.2f
        return pitch % f2
    }
}