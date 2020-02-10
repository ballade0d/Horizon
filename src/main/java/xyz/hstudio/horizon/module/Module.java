package xyz.hstudio.horizon.module;

import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.PlayerViolateEvent;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.data.HoriPlayer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Module<K extends Data, V extends CheckConfig<V>> {

    // Use LinkedHashMap to keep the check order.
    private static final Map<ModuleType, Module<? extends Data, ? extends CheckConfig>> moduleMap = new LinkedHashMap<>(16, 1);

    private final ModuleType moduleType;
    @Getter
    private final V config;

    public Module(final ModuleType moduleType, final V config) {
        // This throws a warn in IDE, tell me any suggestion if you have.
        this.moduleType = moduleType;
        this.config = config.load();
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
    public static void doCheck(final Event event, final HoriPlayer player) {
        for (Module modules : Module.moduleMap.values()) {
            if (!modules.config.enabled) {
                continue;
            }
            if (event.isCancelled()) {
                return;
            }
            modules.doCheck(event, player, modules.getData(player), modules.config);
        }
    }

    /**
     * Punish a player. Still have not been finished yet.
     *
     * @param player The player.
     * @param data   Player's data of this check.
     * @param type   Check type.
     * @param weight Violation level addition.
     * @author MrCraftGoo
     */
    protected void punish(final Event event, final HoriPlayer player, final K data, final String type, final float weight) {
        float oldViolation = data.violations.getOrDefault(type, 0F);
        float nowViolation = oldViolation + weight;

        PlayerViolateEvent violateEvent = new PlayerViolateEvent(player.player, this.moduleType, nowViolation, oldViolation);
        Bukkit.getPluginManager().callEvent(violateEvent);
        if (violateEvent.isCancelled()) {
            return;
        }

        if (nowViolation > config.cancel_vl) {
            this.cancel(event, type, player, data, config);
        }

        for (Map.Entry<Integer, List<String>> entry : config.action.entrySet()) {
            int vl = entry.getKey();
            if (oldViolation < vl && nowViolation >= vl) {
                McAccessor.INSTANCE.ensureMainThread(() -> entry.getValue().forEach(cmd ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)));
                break;
            }
        }

        data.violations.put(type, nowViolation);
        data.lastFailTick = player.currentTick;
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
        float nowViolation = data.violations.getOrDefault(type, 0F);
        nowViolation *= multiplier;
        data.violations.put(type, nowViolation);
    }

    public abstract K getData(final HoriPlayer player);

    public abstract void cancel(final Event event, final String type, final HoriPlayer player, final K data, final V config);

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