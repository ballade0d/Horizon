package xyz.hstudio.horizon.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.data.HoriPlayer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Async extends Thread {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Horizon Processing Thread")
                    .build());

    public static <T> Future<T> submit(final Callable<T> callable) {
        return THREAD_POOL.submit(callable);
    }

    public static void execute(final Runnable command) {
        THREAD_POOL.execute(command);
    }

    public Async() {
        super("Horizon Processing Thread");
        this.setDaemon(true);
        this.start();
    }

    private long currentTick;
    public volatile boolean running = true;

    @Override
    public void run() {
        try {
            long current = System.nanoTime();
            while (running) {

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