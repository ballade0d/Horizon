package xyz.hstudio.horizon.bukkit.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Async {

    private static final ExecutorService threadPool = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Horizon Processing Thread")
                    .build());

    public static Future<?> submit(final Runnable task) {
        return threadPool.submit(task);
    }

    public static void execute(final Runnable command) {
        threadPool.execute(command);
    }
}