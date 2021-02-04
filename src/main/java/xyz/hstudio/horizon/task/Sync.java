package xyz.hstudio.horizon.task;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.util.Location;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Sync extends BukkitRunnable {

    private final Horizon inst;

    public Sync(Horizon inst) {
        this.inst = inst;
    }

    public void start() {
        this.runTaskTimer(inst, 1L, 1L);
    }

    @Override
    public void run() {
        executeTeleports();
        executeTasks();
    }

    /**
     * Sync task executor
     */
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public void runSync(Runnable runnable) {
        tasks.add(runnable);
    }

    private void executeTasks() {
        Runnable runnable;
        while ((runnable = tasks.poll()) != null) {
            runnable.run();
        }
    }

    /**
     * Sync teleporter
     */
    private final Map<HPlayer, Location> pendingTeleports = new ConcurrentHashMap<>();

    public void teleport(HPlayer player, Location to) {
        if (player == null || to == null) {
            return;
        }
        pendingTeleports.put(player, to);
    }

    private void executeTeleports() {
        pendingTeleports.entrySet().removeIf(entry -> entry.getKey().teleportUnsafe(entry.getValue()));
    }
}