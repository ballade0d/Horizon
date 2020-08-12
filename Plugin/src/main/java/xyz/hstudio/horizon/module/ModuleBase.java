package xyz.hstudio.horizon.module;

import lombok.val;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.EnumCheck;
import xyz.hstudio.horizon.event.Event;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ModuleBase {

    public static final Map<EnumCheck, ModuleBase> MODULES = new LinkedHashMap<>();
    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    private final EnumCheck check;
    private final int decayAmount;
    private final int decayDelay;
    private final int decayInterval;

    public ModuleBase(EnumCheck check, int decayAmount, int decayDelay, int decayInterval) {
        this.check = check;
        this.decayAmount = decayAmount;
        this.decayDelay = decayDelay;
        this.decayInterval = decayInterval;

        MODULES.put(check, this);
    }

    public void decay(long tick) {
        if (decayInterval == -1 || tick % decayInterval != 0) {
            return;
        }
        for (HPlayer player : inst.getPlayers().values()) {
            val failTick = player.getFailTicks().get(check);
            if (failTick == null) {
                continue;
            }
            if (inst.getAsync().getTick() - failTick < decayDelay) {
                continue;
            }
            player.getViolations().computeIfPresent(check, (k, v) -> {
                val violation = v - decayAmount;
                return violation <= 0 ? null : violation;
            });
        }
    }

    protected void punish(HPlayer player, Event event, String type, double adder, String... info) {
        val prevViolation = player.getViolations().getOrDefault(check, 0);
        val violation = prevViolation + (int) Math.ceil(adder);

        player.getViolations().put(check, violation);
        player.getFailTicks().put(check, inst.getAsync().getTick());
    }

    protected abstract void check(HPlayer player, Event event);
}