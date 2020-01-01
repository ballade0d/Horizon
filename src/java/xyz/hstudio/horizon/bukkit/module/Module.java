package xyz.hstudio.horizon.bukkit.module;

import lombok.Getter;
import xyz.hstudio.horizon.bukkit.Logger;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.data.Data;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Module<K extends Data, V extends Config> {

    private static final Map<ModuleType, Module> moduleMap = new ConcurrentHashMap<>();

    private final ModuleType moduleType;
    @Getter
    private final V config;

    public Module(final ModuleType moduleType, final V config) {
        // This throws a warn in IDE, tell me any suggestion if you have.
        this.moduleType = moduleType;
        this.config = (V) config.load(moduleType);
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
        for (Module module : Module.moduleMap.values()) {
            if (!module.config.enabled) {
                continue;
            }
            module.doCheck(event, player, module.getData(player), module.config);
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
        int lastVL = data.lastVL = (int) data.vL;
        double vL = data.vL += weight;

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