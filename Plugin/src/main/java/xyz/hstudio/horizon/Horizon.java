package xyz.hstudio.horizon;

import com.google.common.base.Preconditions;
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
    private final Async async = new Async(this);
    @Getter
    private final Sync sync = new Sync(this);

    public Horizon() {
        Preconditions.checkState(
                Version.getInst() != Version.UNKNOWN,
                "Unsupported version!");
    }

    @Override
    public void onEnable() {
        // Async task
        async.start();

        // Sync task
        sync.start();

        // Load static code block
        BlockUtils.isSolid(Material.AIR);

        // Load the config file
        File folder = getDataFolder();
        File configFile = new File(folder, "config.yml");
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

    public void onDisable() {
        // Unregister packet handlers
        players.values().forEach(p -> p.packetHandler.unregister());
        players.clear();

        // Stop the tasks
        async.cancel();
        sync.cancel();

        // Release the jar
        try {
            ((URLClassLoader) getClassLoader()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}