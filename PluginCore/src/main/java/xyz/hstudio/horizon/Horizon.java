package xyz.hstudio.horizon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.hstudio.horizon.command.Executors;
import xyz.hstudio.horizon.compat.IBot;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.ConfigFile;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.kirin.Verification;
import xyz.hstudio.horizon.listener.Listeners;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.module.checks.*;
import xyz.hstudio.horizon.network.ChannelHandler;
import xyz.hstudio.horizon.thread.Async;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.enums.Version;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Horizon extends JavaPlugin {

    // This is a recoding version.
    // You can contribute us if you want to help horizon become the best anticheat.

    public static final Map<String, Object> VALUES = new HashMap<>();

    public static final Map<UUID, HoriPlayer> PLAYERS = new ConcurrentHashMap<>();
    @Getter
    private static Horizon inst;

    public static void addValue(final String path, final Object val) {
        VALUES.put(path, val);
    }

    public final Map<String, LangFile> langMap = new ConcurrentHashMap<>();
    public YamlLoader checkLoader;
    public ConfigFile config;
    public boolean usePapi;
    public boolean useViaVer;
    public boolean usePSupport;

    private BukkitTask syncTask;
    private Async asyncTask;

    public Horizon() {
        Horizon.inst = this;
    }

    @Override
    public void onEnable() {
        if (Version.VERSION == Version.UNKNOWN) {
            // Unknown version
            Logger.msg("Info", "Your server version is not supported!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        File folder = this.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Load config
        File checkFile = new File(folder, "check.yml");
        if (!checkFile.exists()) {
            saveResource("check.yml", true);
        }
        this.checkLoader = YamlLoader.loadConfiguration(checkFile);

        File configFile = new File(folder, "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", true);
        }
        YamlLoader configYaml = YamlLoader.loadConfiguration(configFile);
        this.config = AbstractFile.load(null, new ConfigFile(), configYaml);

        this.langMap.put("original", AbstractFile.load(null, new LangFile(), YamlLoader.loadConfiguration(this.getResource("lang.yml"))));
        // TODO: Load languages from cloud

        this.usePapi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.useViaVer = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        this.usePSupport = Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");

        if (this.config.kirin_enabled) {
            saveResource("pubKey.key", true);
            try {
                new Verification(new File(folder, "pubKey.key"), this.config.kirin_licence);
            } catch (Exception e) {
                Logger.msg("Kirin", "Failed to init Kirin! Please contact the author for help.");
                e.printStackTrace();
            }
        }

        new MetricsLite(this, 4236);

        // Run every 50ms (1 tick)
        this.asyncTask = new Async();
        this.syncTask = Bukkit.getScheduler().runTaskTimer(this, new Sync(), 1L, 1L);

        new Listeners();
        new Executors();
        // Load static code block
        BlockUtils.isSolid(Material.AIR);

        // Enable checks
        new AntiBot();
        new AntiVelocity();
        new AutoClicker();
        new BadPacket();
        new GroundSpoof();
        new HealthTag();
        new HitBox();
        new InvalidMotion();
        new Inventory();
        new KillAura();
        new KillAuraBot();
        new NoSwing();
        new Scaffold();
        new Speed();
        new Timer();

        Bukkit.getOnlinePlayers().forEach(HoriPlayer::new);

        Async.LOG.addLast("Horizon is fully startup.");
    }

    @Override
    public void onDisable() {
        Horizon.PLAYERS.values().forEach(player -> {
            IBot bot = player.killAuraBotData.bot;
            if (bot != null) {
                bot.despawn(player);
                player.killAuraBotData.bot = null;
            }
            ChannelHandler.unregister(player);
        });
        Horizon.PLAYERS.clear();

        Module.MODULE_MAP.clear();

        if (this.syncTask != null) {
            this.syncTask.cancel();
        }
        this.asyncTask.running = false;

        try {
            ((URLClassLoader) this.getClassLoader()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LangFile getLang(final String name) {
        return config.personalized_themes_enabled ?
                langMap.getOrDefault(name, langMap.get("original")) :
                langMap.get("original");
    }
}