package xyz.hstudio.horizon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.configuration.Config;
import xyz.hstudio.horizon.configuration.Execution;
import xyz.hstudio.horizon.module.checks.*;
import xyz.hstudio.horizon.storage.MySQL;
import xyz.hstudio.horizon.task.Async;
import xyz.hstudio.horizon.task.Sync;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Yaml;

import java.io.File;
import java.io.IOException;
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
    @Getter
    private final MySQL sql = new MySQL();

    public Horizon() {
        try {
            Class.forName("net.minecraft.server.v1_8_R3.MinecraftServer");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unsupported version!");
        }
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
        Config.load(Config.class);

        File executionFile = new File(folder, "execution.yml");
        if (!folder.isDirectory() || !executionFile.isFile()) {
            saveResource("execution.yml", true);
        }
        Execution.load(Yaml.loadConfiguration(executionFile));

        // Setup for SQL
        sql.setup();

        // Load the config of checks
        Config.load(AimAssist.class);
        Config.load(AntiVelocity.class);
        Config.load(BadPackets.class);
        Config.load(GroundSpoof.class);
        Config.load(HealthTag.class);
        Config.load(HitBox.class);
        Config.load(KillAura.class);
        Config.load(KillAuraBot.class);
        Config.load(NoSwing.class);
        Config.load(Phase.class);
        Config.load(VerticalMovement.class);

        // Register for joined players
        Bukkit.getOnlinePlayers().forEach(HPlayer::new);

        // Register when player joins
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onJoin(PlayerJoinEvent e) {
                new HPlayer(e.getPlayer());
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onQuit(PlayerQuitEvent e) {
                players.remove(e.getPlayer().getUniqueId());
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onTeleport(PlayerTeleportEvent e) {
                if (e.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN || !Config.GHOST_BLOCK_FIX) {
                    return;
                }

                HPlayer p = players.get(e.getPlayer().getUniqueId());
                if (p == null) {
                    return;
                }
                AABB aabb = new Location(
                        p.world(), e.getTo().getX(), e.getTo().getY(), e.getTo().getZ()
                ).toAABB().expand(0.1, 0.1, 0.1);

                aabb.blocks(p.world()).forEach(b -> p.pipeline.writeAndFlush(p.world().updateBlock(b)));
            }
        }, this);

        try {
            Config.watcher();
        } catch (IOException e) {
            Logger.msg("WARN", "Failed to register WatchService! Stacktrace:");
            e.printStackTrace();
        }
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