package xyz.hstudio.horizon.bukkit.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.compat.McAccess;

import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Async extends TimerTask {

    private static final ExecutorService threadPool = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Horizon Processing Thread")
                    .build());
    private long currentTick;

    public static Future<?> submit(final Runnable task) {
        return threadPool.submit(task);
    }

    public static void execute(final Runnable command) {
        threadPool.execute(command);
    }

    @Override
    public void run() {
        // Run every 60 tick
        if (currentTick % 60 == 0) {
            long time = System.currentTimeMillis();
            Object packet = McAccess.getInst().newTransactionPacket();
            Horizon.PLAYERS.values().forEach(p -> {
                p.lastRequestSent = time;
                p.sendPacket(packet);
            });
        }
        currentTick++;
    }
}