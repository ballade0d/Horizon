package xyz.hstudio.horizon.task;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.hstudio.horizon.Horizon;

public class Sync extends BukkitRunnable {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    public void start() {
        this.runTaskTimer(inst, 1L, 1L);
    }

    @Override
    public void run() {
    }
}