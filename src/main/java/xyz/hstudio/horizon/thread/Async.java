package xyz.hstudio.horizon.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import sun.security.action.GetPropertyAction;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.DateUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessController;
import java.util.Deque;
import java.util.concurrent.*;

public class Async extends Thread {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(2,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Horizon Processing Thread")
                    .build());
    public static final Deque<String> LOG = new ConcurrentLinkedDeque<>();

    public static <T> Future<T> submit(final Callable<T> callable) {
        return THREAD_POOL.submit(callable);
    }

    public static void execute(final Runnable command) {
        THREAD_POOL.execute(command);
    }

    private FileWriter logWriter;
    private String lineSeparator;

    public Async() {
        super("Horizon Processing Thread");
        this.setDaemon(true);

        try {
            File logs = new File(Horizon.getInst().getDataFolder(), "logs");
            if (!logs.exists()) {
                logs.mkdirs();
            }
            String date = DateUtils.now(false);
            File log = new File(logs, date + ".log");
            if (!log.exists()) {
                log.createNewFile();
            }
            this.logWriter = new FileWriter(log, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lineSeparator = AccessController.doPrivileged(new GetPropertyAction("line.separator"));

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

                String time = "[" + DateUtils.now(true) + "] ";
                for (String message : Async.LOG) {
                    this.logWriter.write(time + message);
                    this.logWriter.write(lineSeparator);
                }
                Async.LOG.clear();
                this.logWriter.flush();

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