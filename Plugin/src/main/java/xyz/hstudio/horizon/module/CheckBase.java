package xyz.hstudio.horizon.module;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.OutEvent;

public abstract class CheckBase {

    protected static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    protected final HPlayer p;
    private final int decayAmount;
    private final int decayDelay;
    private final int decayInterval;

    private int violation;
    private int failedTick;

    protected CheckBase(HPlayer p, int decayAmount, int decayDelay, int decayInterval) {
        this.p = p;
        this.decayAmount = decayAmount;
        this.decayDelay = decayDelay;
        this.decayInterval = decayInterval;
    }

    public void decay(long tick) {
        if (decayInterval == -1 || violation == 0) {
            return;
        }
        if (tick % decayInterval != 0L) {
            return;
        }
        if (inst.getAsync().getTick().get() - failedTick < decayDelay) {
            return;
        }
        violation = Math.max(violation - decayAmount, 0);
    }

    protected void punish(InEvent<?> event, String type, float adder, String... info) {
        int violation = this.violation + Math.max(NumberConversions.round(adder), 1);
        this.violation = violation;
        this.failedTick = inst.getAsync().getTick().get();
    }

    public void received(InEvent<?> event) {
    }

    public void sent(OutEvent<?> event) {
    }
}