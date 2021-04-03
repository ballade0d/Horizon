package xyz.hstudio.horizon;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import me.cgoo.api.cfg.Cfg;
import me.cgoo.api.logger.Logger;
import me.cgoo.api.util.YamlUtil;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.command.ConsoleCmd;
import xyz.hstudio.horizon.command.InGameCmd;
import xyz.hstudio.horizon.configuration.Config;
import xyz.hstudio.horizon.configuration.Execution;
import xyz.hstudio.horizon.language.Language;
import xyz.hstudio.horizon.module.checks.*;
import xyz.hstudio.horizon.storage.MySQL;
import xyz.hstudio.horizon.task.Async;
import xyz.hstudio.horizon.task.Sync;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.kirin.Kirin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.sql.SQLException;
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
    @Getter
    private final InGameCmd inGameCmd = new InGameCmd(this);
    @Getter
    private final ConsoleCmd consoleCmd = new ConsoleCmd(this);

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
        try {
            Cfg.watchClz(this, Cfg.load(this, Config.class));
        } catch (IOException e) {
            Logger.warn("Failed to register WatchService for Horizon folder! Stacktrace:");
            e.printStackTrace();
        }

        try {
            Logger.init(this, "Horizon", Config.LOG);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initial the logging system. Reason: " + e.getMessage());
        }

        File executionFile = new File(folder, "execution.yml");
        if (!folder.isDirectory() || !executionFile.isFile()) {
            saveResource("execution.yml", true);
        }
        Execution.load(YamlUtil.load(executionFile));

        // Setup for SQL
        sql.setup();

        // Load the config of checks
        try {
            Cfg.watchClz(this,
                    Cfg.load(this, AimAssist.class),
                    Cfg.load(this, AntiVelocity.class),
                    Cfg.load(this, BadPackets.class),
                    Cfg.load(this, GroundSpoof.class),
                    Cfg.load(this, HealthTag.class),
                    Cfg.load(this, HitBox.class),
                    Cfg.load(this, KillAura.class),
                    Cfg.load(this, KillAuraBot.class),
                    Cfg.load(this, NoSwing.class),
                    Cfg.load(this, Phase.class),
                    Cfg.load(this, VerticalMovement.class));
        } catch (IOException e) {
            Logger.warn("Failed to register WatchService for checks folder! Stacktrace:");
            e.printStackTrace();
        }

        try {
            Cfg.watchObj(this,
                    Cfg.load(this, new Language("en_US"), "language/en_US.yml"));
        } catch (IOException e) {
            Logger.warn("Failed to register WatchService for languages folder! Stacktrace:");
            e.printStackTrace();
        }

        // Register for joined players
        Bukkit.getOnlinePlayers().forEach(HPlayer::new);

        // Register when player joins
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onJoin(PlayerJoinEvent e) {
                HPlayer p = new HPlayer(e.getPlayer());
                try {
                    sql.initData(p);
                } catch (SQLException exception) {
                    Logger.warn("Failed to init sql for player " + e.getPlayer().getName());
                    exception.printStackTrace();
                }
            }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onQuit(PlayerQuitEvent e) {
                UUID uuid = e.getPlayer().getUniqueId();
                HPlayer p = players.get(uuid);
                if (p == null) {
                    return;
                }
                async.clearHistory(p.base);
                players.remove(uuid);
            }

            // The ghost-block remover
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

        Metrics metrics = new Metrics(this, 4236);
        metrics.addCustomChart(new Metrics.SimplePie("kirin", () -> String.valueOf(Kirin.verified)));
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
            ((URLClassLoader) getClassLoader()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeBungeeCommand(HPlayer p, String command) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
        DataOutputStream msgOut = new DataOutputStream(msgBytes);
        try {
            msgOut.writeUTF(command);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        out.writeShort(msgBytes.toByteArray().length);
        out.write(msgBytes.toByteArray());

        Packet<?> packet = new PacketPlayOutCustomPayload("horizon:data_transporter",
                new PacketDataSerializer(Unpooled.wrappedBuffer(out.toByteArray())));
        p.pipeline.writeAndFlush(packet);
    }
}