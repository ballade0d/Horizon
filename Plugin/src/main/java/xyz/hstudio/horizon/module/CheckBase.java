package xyz.hstudio.horizon.module;

import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.OutEvent;

public abstract class CheckBase {

    protected static final Horizon inst = Horizon.getPlugin(Horizon.class);

    protected final HPlayer p;
    private final int decayAmount;
    private final int decayDelay;
    private final int decayInterval;
    private int violation;
    private int failedTick;

    public CheckBase(HPlayer p, int decayAmount, int decayDelay, int decayInterval) {
        this.p = p;
        this.decayAmount = decayAmount;
        this.decayDelay = decayDelay;
        this.decayInterval = decayInterval;
    }

    public void decay(long tick) {
        if (decayInterval == -1 || violation == 0) {
            return;
        }
        if (tick % decayInterval != 0) {
            return;
        }
        if (inst.getAsync().getTick() - failedTick < decayDelay) {
            return;
        }
        this.violation = Math.max(violation - decayAmount, 0);
    }

    protected void punish(InEvent event, String type, float adder, String... info) {
        int violation = this.violation + NumberConversions.ceil(adder);

        this.violation = violation;
        this.failedTick = inst.getAsync().getTick();
    }

    public void received(InEvent event) {
    }

    public void sent(OutEvent event) {
    }
}