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
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        McAccess.init();
        PacketConvert.init();
        new Listeners();

        // Enable checks
        new KillAura();
    }
}