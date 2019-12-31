package xyz.hstudio.horizon.bukkit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.compat.PacketConvert;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.listener.Listeners;
import xyz.hstudio.horizon.bukkit.module.checks.KillAura;
import xyz.hstudio.horizon.bukkit.util.Version;
import xyz.hstudio.horizon.bukkit.util.YamlLoader;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Horizon extends JavaPlugin {

    // This is a recoding version.
    // You can contribute us if you want to help horizon become the best anticheat.

    public static final Map<UUID, HoriPlayer> PLAYERS = new ConcurrentHashMap<>();
    @Getter
    private static Horizon inst;

    public Map<String, YamlLoader> configMap = new ConcurrentHashMap<>();

    public Horizon() {
        Horizon.inst = this;
    }

    @Override
    public void onEnable() {
        if (Version.VERSION == Version.UNKNOWN) {
            // Unknown version
            Logger.info("INFO", "Your server version is not supported!");
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
    }

    @Override
    public void onDisable() {
        for (Map.Entry<String, YamlLoader> entry : this.configMap.entrySet()) {
            try {
                entry.getValue().save(new File(this.getDataFolder(), entry.getKey()));
            } catch (IOException e) {
                Logger.info("ERROR", "Failed to save the file " + entry.getKey() + "!");
            }
        }
    }
}