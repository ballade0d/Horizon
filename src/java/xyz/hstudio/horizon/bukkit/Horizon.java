package xyz.hstudio.horizon.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.compat.PacketConvert;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.listener.Listeners;
import xyz.hstudio.horizon.bukkit.module.checks.KillAura;
import xyz.hstudio.horizon.bukkit.module.checks.Scaffold;
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

    public Horizon() {
        Horizon.inst = this;
    }

    @Override
    public void onEnable() {
        if (Version.VERSION == Version.UNKNOWN) {
            // Unknown version
            Logger.info("Info", "Your server version is not supported!");
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
        YamlLoader yaml = YamlLoader.loadConfiguration(checkFile);
        this.configMap.put("check.yml", yaml);

        McAccess.init();
        PacketConvert.init();
        new Listeners();

        // Enable checks
        new KillAura();
        new Scaffold();

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
                Logger.info("Annc", "Horizon Announcement");
                announcements.forEach(s -> Logger.info("Annc", s));
            } catch (Exception ignore) {
            }
        };
        Async.execute(command);
    }

    @Override
    public void onDisable() {
        for (Map.Entry<String, YamlLoader> entry : this.configMap.entrySet()) {
            try {
                entry.getValue().save(new File(this.getDataFolder(), entry.getKey()));
            } catch (IOException e) {
                Logger.info("Error", "Failed to save the file " + entry.getKey() + "!");
            }
        }
    }
}