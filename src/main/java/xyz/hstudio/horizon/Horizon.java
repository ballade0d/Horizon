package xyz.hstudio.horizon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.hstudio.horizon.command.Commands;
import xyz.hstudio.horizon.config.DefaultConfig;
import xyz.hstudio.horizon.config.Language;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.kirin.Kirin;
import xyz.hstudio.horizon.listener.Listeners;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.module.checks.*;
import xyz.hstudio.horizon.network.ChannelHandler;
import xyz.hstudio.horizon.thread.Async;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.JsonUtils;
import xyz.hstudio.horizon.util.enums.Version;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Horizon extends JavaPlugin {

    // This is a recoding version.
    // You can contribute us if you want to help horizon become the best anticheat.

    public static final Map<UUID, HoriPlayer> PLAYERS = new ConcurrentHashMap<>();
    @Getter
    private static Horizon inst;

    public final Map<String, YamlLoader> configMap = new ConcurrentHashMap<>();
    public JsonArray announcements = new JsonArray();
    public Set<String> aliases = new HashSet<>();
    public DefaultConfig config;
    public Language language;

    private BukkitTask task;
    private Async thread;

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

        File checkFile = new File(folder, "check.yml");
        if (!checkFile.exists()) {
            saveResource("check.yml", true);
        }
        YamlLoader checkYaml = YamlLoader.loadConfiguration(checkFile);
        this.configMap.put("check.yml", checkYaml);

        File configFile = new File(folder, "config.yml");
        if (!checkFile.exists()) {
            saveResource("config.yml", true);
        }
        YamlLoader configYaml = YamlLoader.loadConfiguration(configFile);
        this.configMap.put("config.yml", configYaml);
        this.config = new DefaultConfig().load();

        File langFile = new File(folder, "language.yml");
        if (!langFile.exists()) {
            saveResource("language.yml", true);
        }
        YamlLoader langYaml = YamlLoader.loadConfiguration(langFile);
        this.configMap.put("language.yml", langYaml);
        this.language = new Language().load();

        if (this.config.kirin_enabled) {
            try {
                new Kirin(this.config.kirin_licence);
            } catch (Exception e) {
                Logger.msg("Kirin", "Failed to init Kirin! Please contact the author for help.");
                e.printStackTrace();
            }
        }

        // Run every 50ms (1 tick)
        this.thread = new Async();
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Sync(), 1L, 1L);

        new Listeners();
        new Commands();

        // Enable checks
        new AntiVelocity();
        new BadPacket();
        new GroundSpoof();
        new HitBox();
        new InvalidMotion();
        new Inventory();
        new KillAura();
        new NoSwing();
        new Scaffold();
        new Speed();
        new Timer();

        // Enable commands
        // Do not need to cache exceptions.
        // If the command is deleted in plugin.yml, it will throw a NPE and plugin won't start.
        this.aliases.add("horizon");
        this.aliases.addAll(Bukkit.getPluginCommand("horizon").getAliases());
        this.aliases = this.aliases.stream().map(s -> "/" + s.toLowerCase()).collect(Collectors.toSet());

        // Get announcements from the official server.
        Runnable command = () -> {
            try {
                JsonObject object = JsonUtils.readAsObject(new URL("https://horizon.hstudio.xyz/horizon/announcement.json"));
                this.announcements = object.getAsJsonArray("messages");
                Logger.msg("Annc", "Horizon Announcement");
                this.announcements.forEach(s -> Logger.msg("Annc", s.getAsString()));
            } catch (Exception ignore) {
            }
        };
        Async.execute(command);
    }

    @Override
    public void onDisable() {
        Horizon.PLAYERS.values().forEach(ChannelHandler::unregister);

        Module.MODULE_MAP.clear();

        this.task.cancel();
        this.thread.running = false;
    }
}