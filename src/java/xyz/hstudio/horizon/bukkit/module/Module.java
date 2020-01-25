package xyz.hstudio.horizon.bukkit.module;

import lombok.Getter;
import xyz.hstudio.horizon.bukkit.Logger;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.data.Data;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.util.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Module<K extends Data, V extends Config> {

    private static final Map<ModuleType, Module<? extends Data, ? extends Config>> moduleMap = new ConcurrentHashMap<>();

    private final ModuleType moduleType;
    @Getter
    private final V config;

    public Module(final ModuleType moduleType, final V config) {
        // This throws a warn in IDE, tell me any suggestion if you have.
        this.moduleType = moduleType;
        this.config = Config.load(moduleType, config);
        Module.moduleMap.put(moduleType, this);
    }

    /**
     * Execute all checks.
     * This throws a warn in IDE, tell me any suggestion if you have.
     *
     * @param event  The event.
     * @param player The player.
     * @return Should the packet of the event pass.
     * @author MrCraftGoo
     */
    public static boolean doCheck(final Event event, final HoriPlayer player) {
        for (Module modules : Module.moduleMap.values()) {
            if (!modules.config.enabled) {
                continue;
            }
            modules.doCheck(event, player, modules.getData(player), modules.config);
        }
        return true;
    }

    /**
     * Punish a player. Still have not been finished yet.
     *
     * @param player   The player.
     * @param data     Player's data of this check.
     * @param type     Check type.
     * @param weight   Violation level addition.
     * @param runnable The tasks will be executed.
     * @author MrCraftGoo
     */
    protected void punish(final HoriPlayer player, final K data, final String type, final double weight, final Runnable... runnable) {
        Pair<Double, Double> pair = data.violationLevels.getOrDefault(type, new Pair<>(0D, 0D));
        data.violationLevels.put(type, pair.setValue(pair.getKey()).setKey(pair.getKey() + weight));

        int prevVL = (int) data.violationLevels.values().stream().mapToDouble(Pair::getValue).sum();
        int vL = (int) data.violationLevels.values().stream().mapToDouble(Pair::getKey).sum();

        data.lastFailTick = player.currentTick;
        // TODO: Punish
    }

    /**
     * Send a debug message to the console
     */
    protected void debug(final Object object) {
        if (!this.config.debug) {
            return;
        }
        Logger.info("Debug|" + this.moduleType.name(), object);
    }

    protected void reward(final String type, final K data, final double multiplier) {
        Pair<Double, Double> pair = data.violationLevels.get(type);
        if (pair == null) {
            return;
        }
        data.violationLevels.put(type, pair.setValue(pair.getKey()).setKey(pair.getKey() * multiplier));
    }

    public abstract K getData(final HoriPlayer player);

    public abstract void doCheck(final Event event, final HoriPlayer player, final K data, final V config);

    /**
     * This will run every tick (async) to execute some checks.
     *
     * @param currentTick Current ticks
     * @param config      The check config
     * @author MrCraftGoo
     */
    public void runAsync(final long currentTick, final V config) {
    }
}