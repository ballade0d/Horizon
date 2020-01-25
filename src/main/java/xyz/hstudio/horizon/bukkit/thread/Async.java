package xyz.hstudio.horizon.bukkit.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Async implements Runnable {

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
        try {
            long current = System.nanoTime();
            while (true) {

                // Run every 80 tick
                if (currentTick % 80 == 0) {
                    Horizon.PLAYERS.values().forEach(HoriPlayer::sendRequest);
                }
                currentTick++;

                current = (current + 50000000L - System.nanoTime()) / 1000000L;
                // Wait for 1 tick
                if (current > 0) {
                    Thread.sleep(current);
                }
                current = System.nanoTime();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}