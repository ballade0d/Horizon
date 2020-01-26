package xyz.hstudio.horizon.bukkit.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;

import java.util.concurrent.*;

public class Async implements Runnable {

    private static final ExecutorService THREAD_POOL = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Horizon Processing Thread")
                    .build());

    public static Future<?> submit(final Runnable task) {
        return THREAD_POOL.submit(task);
    }

    public static void execute(final Runnable command) {
        THREAD_POOL.execute(command);
    }

    private long currentTick;

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