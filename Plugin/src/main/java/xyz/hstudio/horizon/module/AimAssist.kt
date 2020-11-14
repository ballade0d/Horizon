package xyz.hstudio.horizon.module

import xyz.hstudio.horizon.HPlayer
import xyz.hstudio.horizon.event.InEvent
import xyz.hstudio.horizon.event.inbound.MoveEvent
import xyz.hstudio.horizon.util.MathUtils
import kotlin.math.abs
import kotlin.math.pow

class AimAssist(p: HPlayer) : CheckBase(p, 1, 200, 200) {

    private var lastDeltaPitch = 0f

    override fun received(event: InEvent?) {
        if (event is MoveEvent) gcd(event)
    }

    private fun gcd(e: MoveEvent) {
        if (e.teleport || abs(e.to.pitch) >= 89f) {
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

        val sensitivity = sensToPercent(gcdToSensitive(gcd))

        fun modulusRotation(sensitivity: Double, pitch: Double): Double {
            val f = (sensitivity * 0.6000000238418579 + 0.20000000298023224).toFloat()
            val f2 = f.pow(3) * 1.2f
            return pitch % f2
        }

        val o: Double = modulusRotation(sensitivity, pitch.toDouble())
        val len = o.toString().length
        if (o == 0.0) {
            // Add more vl
            println("Invalid A")
        }
        if (sensitivity > 99 && o > 0 && len > 0 && len < 8) {
            println("Invalid B len:$len")
        }

        lastDeltaPitch = deltaPitch
    }

    private fun gcdToSensitive(gcd: Float): Double = (Math.cbrt(gcd / 0.15 / 8.0) - 0.2) / 0.6

    private fun sensToPercent(sensitivity: Double): Double = sensitivity / 0.5 * 100
}