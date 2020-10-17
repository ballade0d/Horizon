package xyz.hstudio.horizon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.configuration.Config;
import xyz.hstudio.horizon.task.Async;
import xyz.hstudio.horizon.task.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.Yaml;
import xyz.hstudio.horizon.util.enums.Version;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Horizon extends JavaPlugin {

    @Getter
    private final Map<UUID, HPlayer> players = new ConcurrentHashMap<>();
    @Getter
    private Async async;
    @Getter
    private Sync sync;

    public Horizon() {
        // Check the server version first
        if (Version.getInst() == Version.UNKNOWN) {
            throw new IllegalStateException("Unsupported version!");
        }
    }

    @Override
    public void onEnable() {
        // Async task
        (async = new Async()).start();

        // Sync task
        (sync = new Sync()).start();

        // Load static code block
        BlockUtils.isSolid(Material.AIR);

        // Load the config file
        File folder = getDataFolder(), configFile = new File(folder, "config.yml");
        if (!folder.isDirectory() || !configFile.isFile()) {
            saveResource("config.yml", true);
        }
        Config.load(Yaml.loadConfiguration(configFile));

        // Register for joined players
        Bukkit.getOnlinePlayers().forEach(HPlayer::new);

        // Register when player joins
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {
                new HPlayer(e.getPlayer());
            }
        }, this);
    }

    @Override
    public void onDisable() {
        // Unregister packet handlers
        players.values().forEach(p -> p.packetHandler.unregister());
        players.clear();

        // Stop the tasks
        async.cancel();
        sync.cancel();

        // Release the jar
        try {
            ((URLClassLoader) this.getClassLoader()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}