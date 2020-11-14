package xyz.hstudio.horizon.module

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.NumberConversions
import xyz.hstudio.horizon.HPlayer
import xyz.hstudio.horizon.Horizon
import xyz.hstudio.horizon.event.InEvent
import xyz.hstudio.horizon.event.OutEvent

abstract class CheckBase(protected val p: HPlayer, private val decayAmount: Int, private val decayDelay: Int, private val decayInterval: Int) {

    private var violation = 0
    private var failedTick = 0

    fun decay(tick: Long) {
        if (decayInterval == -1 || violation == 0) return
        if (tick % decayInterval != 0L) return
        if (inst.async.tick.get() - failedTick < decayDelay) return
        violation = (violation - decayAmount).coerceAtLeast(0)
    }

    protected fun punish(event: InEvent, type: String, adder: Float, vararg info: String?) {
        val violation = violation + NumberConversions.round(adder.toDouble()).coerceAtLeast(1)
        this.violation = violation
        failedTick = inst.async.tick.get()
    }

    open fun received(event: InEvent?) {}

    open fun sent(event: OutEvent?) {}

    companion object {
        @JvmStatic
        protected val inst: Horizon = JavaPlugin.getPlugin(Horizon::class.java)
    }
}