package xyz.hstudio.horizon.module;

import gnu.trove.map.TIntObjectMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.ConfigBase;
import xyz.hstudio.horizon.configuration.Execution;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.util.Yaml;

import java.io.File;
import java.util.List;

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

    protected CheckBase(HPlayer p) {
        this.p = p;
        this.decayAmount = 0;
        this.decayDelay = -1;
        this.decayInterval = -1;
    }

    public static void init(String cfgName, Class<? extends ConfigBase> clazz, Yaml def) {
        File file = new File(inst.getDataFolder(), "checks/" + cfgName + ".yml");
        if (!file.isFile()) {
            inst.saveResource("checks/" + cfgName + ".yml", true);
        }
        ConfigBase.load(clazz, Yaml.loadConfiguration(file), def);
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

    protected void punish(InEvent<?> event, String type, double adder, Detection detection, String info) {
        int vl = this.violation + Math.max(NumberConversions.round(adder), 1);

        TIntObjectMap<List<String>> action = Execution.getActionMap(detection);
        for (int matcher : action.keys()) {
            if (!(this.violation < matcher && vl >= matcher)) {
                continue;
            }
            inst.getSync().runSync(() -> {
                for (String command : action.get(matcher)) {
                    command = command.replace("%player%", p.bukkit.getName());
                    command = command.replace("%id%", RandomStringUtils.randomAlphanumeric(6));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            });
            break;
        }

        p.bukkit.sendMessage(type + " " + info);

        this.violation = vl;
        this.failedTick = inst.getAsync().getTick();
    }

    public void received(InEvent<?> event) {
    }

    public void sent(OutEvent<?> event) {
    }

    public void tickAsync(int tick) {
    }
}