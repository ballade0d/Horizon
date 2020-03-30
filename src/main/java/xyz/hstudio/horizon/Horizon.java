package xyz.hstudio.horizon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.hstudio.horizon.api.custom.CustomCheck;
import xyz.hstudio.horizon.api.custom.CustomConfig;
import xyz.hstudio.horizon.command.Commands;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.ConfigFile;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.kirin.Kirin;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Horizon extends JavaPlugin {

    // This is a recoding version.
    // You can contribute us if you want to help horizon become the best anticheat.

    public static final Map<UUID, HoriPlayer> PLAYERS = new ConcurrentHashMap<>();
    @Getter
    private static Horizon inst;

    public final Map<String, LangFile> langMap = new ConcurrentHashMap<>();
    public YamlLoader checkLoader;
    public ConfigFile config;
    public boolean usePapi;
    public boolean useViaVer;

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
        this.usePapi = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");

        if (this.config.kirin_enabled) {
            try {
                new Kirin(this.config.kirin_licence);
            } catch (Exception e) {
                Logger.msg("Kirin", "Failed to init Kirin! Please contact the author for help.");
                e.printStackTrace();
            }
        }

        File checkFolder = new File(folder, "checks");
        if (!checkFolder.exists()) {
            checkFolder.mkdirs();
        }
        File[] checks = checkFolder.listFiles();
        if (checks != null) {
            for (File check : checks) {
                String name = check.getName().substring(0, check.getName().lastIndexOf("."));
                try {
                    Object instance = this.loadCheck(check, name);
                    if (!(instance instanceof CustomCheck)) {
                        Logger.msg("Warn", "Custom check " + name + " must implement CustomCheck");
                        continue;
                    }
                    Module.CUSTOM_CHECKS.add((CustomCheck<? extends CustomConfig>) instance);
                    Logger.msg("Info", "Custom check " + name + " is loaded.");
                } catch (Exception e) {
                    Logger.msg("Warn", "Could not load custom check " + name);
                    e.printStackTrace();
                }
            }
        }

        new MetricsLite(this, 4236);

        // Run every 50ms (1 tick)
        this.asyncTask = new Async();
        this.syncTask = Bukkit.getScheduler().runTaskTimer(this, new Sync(), 1L, 1L);

        new Listeners();
        new Commands();
        // Load static code block
        new BlockUtils();

        // Enable checks
        new AntiVelocity();
        new BadPacket();
        new GroundSpoof();
        new HealthTag();
        new HitBox();
        new InvalidMotion();
        new Inventory();
        new KillAura();
        new NoSwing();
        new Scaffold();
        new Speed();
        new Timer();

        Bukkit.getOnlinePlayers().forEach(HoriPlayer::new);

        Async.LOG.addLast("Horizon is fully startup.");
    }

    @Override
    public void onDisable() {
        Horizon.PLAYERS.values().forEach(ChannelHandler::unregister);

        Module.MODULE_MAP.clear();

        if (this.syncTask != null) {
            this.syncTask.cancel();
        }
        this.asyncTask.running = false;
    }

    public LangFile getLang(final String name) {
        return config.personalized_themes_enabled ?
                langMap.getOrDefault(name, langMap.get("original")) :
                langMap.get("original");
    }

    private Object loadCheck(final File file, final String name) throws Exception {
        ClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClassLoader());
        return loader.loadClass(name).newInstance();
    }
}