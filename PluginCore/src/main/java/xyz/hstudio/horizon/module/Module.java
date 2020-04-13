package xyz.hstudio.horizon.module;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.PlayerViolateEvent;
import xyz.hstudio.horizon.api.custom.CustomCheck;
import xyz.hstudio.horizon.api.custom.CustomConfig;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.thread.Async;

import java.util.*;

public abstract class Module<K extends Data, V extends CheckFile> {

    public static final Map<ModuleType, Module<? extends Data, ? extends CheckFile>> MODULE_MAP = new LinkedHashMap<>(16, 1);
    public static final List<CustomCheck<? extends CustomConfig>> CUSTOM_CHECKS = new LinkedList<>();

    private final ModuleType moduleType;
    @Getter
    private final V config;
    private final String[] types;

    public Module(final ModuleType moduleType, final V config, final String... types) {
        this.moduleType = moduleType;
        this.config = AbstractFile.load(moduleType.name().toLowerCase(), config, Horizon.getInst().checkLoader);
        this.types = types;
        Module.MODULE_MAP.put(moduleType, this);
    }

    /**
     * Execute all checks.
     * This throws a warn in IDE, tell me any suggestion if you have.
     *
     * @param event  The event.
     * @param player The player.
     * @author MrCraftGoo
     */
    public static void doCheck(final Event event, final HoriPlayer player) {
        for (Module modules : Module.MODULE_MAP.values()) {
            if (!modules.config.enabled) {
                continue;
            }
            if (!modules.config.enable_worlds.contains("*") &&
                    !modules.config.enable_worlds.contains(player.position.world.getName())) {
                continue;
            }
            if (event.isCancelled()) {
                return;
            }
            modules.doCheck(event, player, modules.getData(player), modules.config);
        }
    }

    /**
     * Punish a player.
     */
    protected void punish(final Event event, final HoriPlayer player, final K data, final int type, final float weight, final String... args) {
        this.punish(event, player, data, type, weight, false, args);
    }

    /**
     * Punish a player.
     */
    protected void punish(final Event event, final HoriPlayer player, final K data, final int type, final float weight, final boolean cancel, final String... args) {
        float oldViolation = data.violations.getOrDefault(type, 0F);
        float nowViolation = oldViolation + weight;

        PlayerViolateEvent violateEvent = new PlayerViolateEvent(player.player, this.moduleType, nowViolation, oldViolation);
        Bukkit.getPluginManager().callEvent(violateEvent);
        if (violateEvent.isCancelled()) {
            return;
        }

        if (cancel || (nowViolation > config.cancel_vl && config.cancel_vl != -1)) {
            this.cancel(event, type, player, data, config);
        }

        for (Map.Entry<Integer, List<String>> entry : config.action.entrySet()) {
            int vl = entry.getKey();
            if (!(oldViolation < vl && nowViolation >= vl)) {
                continue;
            }
            McAccessor.INSTANCE.ensureMainThread(() -> {
                for (String rawCmd : entry.getValue()) {
                    String cmd = rawCmd
                            .replace("%player%", player.player.getName());
                    cmd = Horizon.getInst().usePapi ? PlaceholderAPI.setPlaceholders(player.player, cmd) : cmd;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            });
            Async.LOG.addLast("Executed action \"" + Arrays.toString(entry.getValue().toArray()) + "\". Check:" + moduleType.name() + " VL:" + nowViolation);
            break;
        }

        if (player.verbose || player.player.hasPermission("horizon.verbose")) {
            player.sendMessage(Horizon.getInst().config.prefix + player.getLang().verbose
                    .replace("%player%", player.player.getName())
                    .replace("%check%", this.moduleType.name())
                    .replace("%type%", this.types[type])
                    .replace("%vl_total%", String.valueOf(nowViolation))
                    .replace("%vl_addition%", String.valueOf(weight))
                    .replace("%ping%", String.valueOf(player.ping))
                    .replace("%args%", Arrays.toString(args)));
        }

        if (Horizon.getInst().config.log) {
            Async.LOG.addLast(Horizon.getInst().getLang(Horizon.getInst().config.personalized_themes_default_lang).verbose
                    .replace("%player%", player.player.getName())
                    .replace("%check%", this.moduleType.name())
                    .replace("%type%", this.types[type])
                    .replace("%vl_total%", String.valueOf(nowViolation))
                    .replace("%vl_addition%", String.valueOf(weight))
                    .replace("%ping%", String.valueOf(player.ping))
                    .replace("%args%", Arrays.toString(args)));
        }

        data.violations.put(type, nowViolation);
        data.lastFailTick = player.currentTick;
    }

    protected void reward(final int type, final K data, final double multiplier) {
        float nowViolation = data.violations.getOrDefault(type, 0F);
        nowViolation *= multiplier;
        data.violations.put(type, nowViolation);
    }

    public abstract K getData(HoriPlayer player);

    public abstract void cancel(Event event, int type, HoriPlayer player, K data, V config);

    public abstract void doCheck(Event event, HoriPlayer player, K data, V config);

    public void tickAsync(final long currentTick, final V config) {
    }
}