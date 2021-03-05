package xyz.hstudio.horizon.module;

import gnu.trove.map.TIntObjectMap;
import lombok.Getter;
import lombok.Setter;
import me.cgoo.api.logger.Logger;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.api.event.PlayerViolationEvent;
import xyz.hstudio.horizon.configuration.Execution;
import xyz.hstudio.horizon.event.Event;

import java.sql.SQLException;
import java.util.List;

public abstract class CheckBase {

    protected static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    protected final HPlayer p;
    private final int decayAmount;
    private final int decayDelay;
    private final int decayInterval;

    @Getter
    @Setter
    private int violation;
    private int failedTick;

    protected CheckBase(HPlayer p, int decayAmount, int decayDelay, int decayInterval) {
        this.p = p;
        this.decayAmount = decayAmount;
        this.decayDelay = decayDelay;
        this.decayInterval = decayInterval;
    }

    protected CheckBase(HPlayer p) {
        this.p = p;
        this.decayAmount = 0;
        this.decayDelay = -1;
        this.decayInterval = -1;
    }

    public void decay(long tick) {
        if (decayInterval == -1 || violation == 0) {
            return;
        }
        if (tick % decayInterval != 0L) {
            return;
        }
        if (inst.getAsync().getTick() - failedTick < decayDelay) {
            return;
        }
        violation = Math.max(violation - decayAmount, 0);
    }

    protected void punish(Event<?> event, String type, double adder, Detection detection, String info) {
        int vl = this.violation + Math.max(NumberConversions.round(adder), 1);

        PlayerViolationEvent api = new PlayerViolationEvent(p.nms.getBukkitEntity(), detection, type, vl, info);
        Bukkit.getPluginManager().callEvent(api);
        if (api.isCancelled()) {
            return;
        }

        TIntObjectMap<List<String>> action = Execution.getActionMap(detection);
        for (int matcher : action.keys()) {
            if (!(this.violation < matcher && vl >= matcher)) {
                continue;
            }
            // Commands must be executed in the main thread
            inst.getSync().runSync(() -> {
                for (String command : action.get(matcher)) {
                    command = command.replace("%player%", p.nms.getName());
                    command = command.replace("%id%", RandomStringUtils.randomAlphanumeric(6));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            });
            break;
        }

        p.sendMessage(type + " " + info);
        try {
            inst.getSql().syncData(p, detection, vl);
        } catch (SQLException exception) {
            Logger.warn("Failed to sync the data of player " + p.nms.getName());
            exception.printStackTrace();
        }

        this.violation = vl;
        this.failedTick = inst.getAsync().getTick();
    }

    public void run(Event<?> event) {
    }

    public void tickAsync(int tick) {
    }
}