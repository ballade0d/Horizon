package xyz.hstudio.horizon.task;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.util.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Sync extends BukkitRunnable {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);
    private static final Map<HPlayer, Location> pendingTeleports = new ConcurrentHashMap<>();

    public static void teleport(HPlayer player, final Location to) {
        if (player == null || to == null) {
            return;
        }
        pendingTeleports.put(player, to);
    }

    public void start() {
        this.runTaskTimer(inst, 1L, 1L);
    }

    @Override
    public void run() {
        pendingTeleports.forEach((p, loc) -> p.bukkit.teleport(loc.toBukkit()));
        pendingTeleports.clear();
    }
}