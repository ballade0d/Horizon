package xyz.hstudio.horizon.module.check;

import lombok.val;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.EnumCheckType;
import xyz.hstudio.horizon.event.Event;

public abstract class CheckBase {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    private final HPlayer p;
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

        p.getChecks().add(this);
    }

    public void decay(long tick) {
        if (decayInterval == -1 || tick % decayInterval != 0) {
            return;
        }
        if (inst.getAsync().getTick() - failedTick < decayDelay) {
            return;
        }
        this.violation = Math.max(violation - decayAmount, 0);
    }

    protected void punish(Event event, String type, float adder, String... info) {
        val violation = this.violation + Math.max(1, Math.round(adder));

        this.violation = violation;
        this.failedTick = inst.getAsync().getTick();
    }

    public abstract EnumCheckType getType();

    protected abstract void check(Event event);
}