package xyz.hstudio.horizon.module;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.thread.Async;

import java.util.*;

public abstract class Module<K extends Data, V extends CheckNode> {

    public static final Map<ModuleType, Module<? extends Data, ? extends CheckNode>> MODULE_MAP = new LinkedHashMap<>(16, 1);
    public static final List<CustomCheck<? extends CustomConfig>> CUSTOM_CHECKS = new LinkedList<>();

    private static final String[] ARGS = new String[]{
            "%player%",
            "%check%",
            "%type%",
            "%vl_total%",
            "%vl_addition%",
            "%ping%",
            "%args%"
    };

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
        if (player.getPlayer().isOp() && Horizon.getInst().config.op_bypass) {
            return;
        }
        for (Module module : Module.MODULE_MAP.values()) {
            if (!module.config.enabled) {
                continue;
            }
            if (module.config.disable_worlds.contains(player.position.world.getName())) {
                continue;
            }
            if (module.canBypass(player)) {
                continue;
            }
            if (event.isCancelled()) {
                return;
            }
            module.doCheck(event, player, module.getData(player), module.config);
        }
    }

    protected boolean canBypass(final HoriPlayer player) {
        return player.getPlayer().hasPermission("horizon.bypass." + this.moduleType.name());
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
        Player bPlayer = player.getPlayer();

        float oldViolation = data.violations.getOrDefault(type, 0F);
        float nowViolation = oldViolation + weight;

        PlayerViolateEvent violateEvent = new PlayerViolateEvent(bPlayer, this.moduleType, nowViolation, oldViolation);
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
                    String cmd = StringUtils.replace(rawCmd, "%player%", bPlayer.getName());
                    cmd = Horizon.getInst().usePapi ? PlaceholderAPI.setPlaceholders(bPlayer, cmd) : cmd;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            });
            Async.LOG.addLast("Executed action \"" + Arrays.toString(entry.getValue().toArray()) + "\". Check:" + moduleType.name() + " VL:" + nowViolation);
            break;
        }

        if (player.verbose || bPlayer.hasPermission("horizon.verbose")) {
            String verbose = StringUtils.replaceEach(
                    player.getLang().verbose,
                    ARGS,
                    new String[]{bPlayer.getName(), this.moduleType.name(),
                            this.types[type], String.valueOf(nowViolation),
                            String.valueOf(weight), String.valueOf(player.ping),
                            Arrays.toString(args)
                    });
            player.sendMessage(Horizon.getInst().config.prefix + verbose);
        }

        if (Horizon.getInst().config.log) {
            String verbose = StringUtils.replaceEach(
                    Horizon.getInst().getLang(Horizon.getInst().config.default_lang).verbose,
                    ARGS,
                    new String[]{
                            bPlayer.getName(), this.moduleType.name(),
                            this.types[type], String.valueOf(nowViolation),
                            String.valueOf(weight), String.valueOf(player.ping),
                            Arrays.toString(args)
                    });
            Async.LOG.addLast(verbose);
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