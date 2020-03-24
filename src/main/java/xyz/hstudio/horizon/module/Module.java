package xyz.hstudio.horizon.module;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.PlayerViolateEvent;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.thread.Async;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Module<K extends Data, V extends CheckFile> {

    // Use LinkedHashMap to keep the check order.
    public static final Map<ModuleType, Module<? extends Data, ? extends CheckFile>> MODULE_MAP = new LinkedHashMap<>(16, 1);

    private final ModuleType moduleType;
    @Getter
    private final V config;

    public Module(final ModuleType moduleType, final V config) {
        this.moduleType = moduleType;
        this.config = AbstractFile.load(moduleType.name().toLowerCase(), config, Horizon.getInst().checkLoader);
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
    protected void punish(final Event event, final HoriPlayer player, final K data, final String type, final float weight, final String... args) {
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
            break;
        }

        if (true) {
            player.sendMessage(Horizon.getInst().config.prefix + player.getLang().verbose
                    .replace("%player%", player.player.getName())
                    .replace("%check%", this.moduleType.name())
                    .replace("%type%", type)
                    .replace("%vl_total%", String.valueOf(nowViolation))
                    .replace("%vl_addition%", String.valueOf(weight))
                    .replace("%ping%", String.valueOf(player.ping))
                    .replace("%args%", Arrays.toString(args)));
        }

        if (Horizon.getInst().config.log) {
            Async.LOG.addLast(Horizon.getInst().getLang(Horizon.getInst().config.personalized_themes_default_lang).verbose
                    .replace("%player%", player.player.getName())
                    .replace("%check%", this.moduleType.name())
                    .replace("%type%", type)
                    .replace("%vl_total%", String.valueOf(nowViolation))
                    .replace("%vl_addition%", String.valueOf(weight))
                    .replace("%ping%", String.valueOf(player.ping))
                    .replace("%args%", Arrays.toString(args)));
        }

        data.violations.put(type, nowViolation);
        data.lastFailTick = player.currentTick;
    }

    protected void reward(final String type, final K data, final double multiplier) {
        float nowViolation = data.violations.getOrDefault(type, 0F);
        nowViolation *= multiplier;
        data.violations.put(type, nowViolation);
    }

    public abstract K getData(final HoriPlayer player);

    public abstract void cancel(final Event event, final String type, final HoriPlayer player, final K data, final V config);

    public abstract void doCheck(final Event event, final HoriPlayer player, final K data, final V config);
}