package xyz.hstudio.horizon.module;

import lombok.Getter;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.Config;
import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.util.collect.Pair;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Module<K extends Data, V extends Config> {

    // Use LinkedHashMap to keep the check order.
    private static final Map<ModuleType, Module<? extends Data, ? extends Config>> moduleMap = new LinkedHashMap<>(16, 1);

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
        pair.value = pair.key;
        pair.key = pair.key + weight;
        data.violationLevels.put(type, pair);

        int prevVL = (int) data.violationLevels.values().stream().mapToDouble(p -> p.value).sum();
        int vL = (int) data.violationLevels.values().stream().mapToDouble(p -> p.key).sum();

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
        Logger.msg("Debug|" + this.moduleType.name(), object);
    }

    protected void reward(final String type, final K data, final double multiplier) {
        Pair<Double, Double> pair = data.violationLevels.get(type);
        if (pair == null) {
            return;
        }
        pair.value = pair.key;
        pair.key = pair.key * multiplier;
        data.violationLevels.put(type, pair);
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