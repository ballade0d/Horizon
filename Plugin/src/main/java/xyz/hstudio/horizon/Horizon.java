package xyz.hstudio.horizon;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.task.Async;
import xyz.hstudio.horizon.task.Sync;
import xyz.hstudio.horizon.util.EnumVersion;

import java.net.URLClassLoader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Horizon extends JavaPlugin {

    @Getter
    private final Map<UUID, HPlayer> players = new ConcurrentHashMap<>();
    @Getter
    private final Async async = new Async(this);
    @Getter
    private final Sync sync = new Sync(this);

    public Horizon() {
        if (EnumVersion.VERSION == EnumVersion.UNKNOWN) {
            throw new IllegalStateException("Unsupported version!");
        }
    }

    @Override
    public void onEnable() {
        Thread asyncThread = new Thread(async, "Horizon Async Processing Thread");
        asyncThread.setDaemon(true);
        asyncThread.start();

        Bukkit.getScheduler().runTaskTimer(this, sync, 1L, 1L);

        Bukkit.getOnlinePlayers().forEach(HPlayer::new);
    }

    @Override
    public void onDisable() {
        try {
            ((URLClassLoader) this.getClassLoader()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}