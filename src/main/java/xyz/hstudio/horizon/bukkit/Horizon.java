package xyz.hstudio.horizon.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.hstudio.horizon.bukkit.config.ConfigFile;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.kirin.Kirin;
import xyz.hstudio.horizon.bukkit.listener.Listeners;
import xyz.hstudio.horizon.bukkit.module.checks.Timer;
import xyz.hstudio.horizon.bukkit.module.checks.*;
import xyz.hstudio.horizon.bukkit.network.ChannelHandler;
import xyz.hstudio.horizon.bukkit.thread.Async;
import xyz.hstudio.horizon.bukkit.util.Version;
import xyz.hstudio.horizon.bukkit.util.YamlLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Horizon extends JavaPlugin {

    // This is a recoding version.
    // You can contribute us if you want to help horizon become the best anticheat.

    public static final Map<UUID, HoriPlayer> PLAYERS = new ConcurrentHashMap<>();
    @Getter
    private static Horizon inst;

    public final Map<String, YamlLoader> configMap = new ConcurrentHashMap<>();
    public List<String> announcements = new ArrayList<>();
    public Set<String> aliases = new HashSet<>();
    public ConfigFile config;

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
        this.config = new ConfigFile().load();

        if (this.config.kirin_enabled) {
            try {
                new Kirin(this.config);
            } catch (Exception e) {
                Logger.msg("Kirin", "Failed to init Kirin! Please contact the author for help.");
                e.printStackTrace();
            }
        }

        // Run every 50ms (1 tick)
        Thread thread = new Thread(new Async(), "Horizon Processing Thread");
        thread.setDaemon(true);
        thread.start();
        new Listeners();

        // Enable checks
        new BadPacket();
        new GroundSpoof();
        new HitBox();
        new InvalidMotion();
        new Inventory();
        new KillAura();
        new Scaffold();
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
                URL url = new URL("https://horizon.hstudio.xyz/horizon/announcement.json");
                InputStreamReader stream = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(stream);
                JSONParser parser = new JSONParser();
                JSONObject object = (JSONObject) parser.parse(reader);
                this.announcements = (List<String>) object.get("messages");
                Logger.msg("Annc", "Horizon Announcement");
                announcements.forEach(s -> Logger.msg("Annc", s));
            } catch (Exception ignore) {
            }
        };
        Async.execute(command);
    }

    @Override
    public void onDisable() {
        Horizon.PLAYERS.values().forEach(ChannelHandler::unregister);
        this.configMap.forEach((string, yaml) -> {
            try {
                yaml.save(new File(this.getDataFolder(), string));
            } catch (IOException e) {
                Logger.msg("Error", "Failed to save the file " + string + "!");
            }
        });
    }
}