package xyz.hstudio.horizon.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.DateUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class Async implements Runnable {

    public static final ScheduledExecutorService THREAD_POOL =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Horizon Processing Thread")
                    .build());

    public static final Deque<String> LOG = new ConcurrentLinkedDeque<>();

    private final FileOutputStream logOutput;
    private final byte[] lineSeparator;

    public Async() {
        try {
            File logs = new File(Horizon.getInst().getDataFolder(), "logs");
            if (!logs.exists()) {
                logs.mkdirs();
            }
            File[] files = logs.listFiles();
            if (files == null) {
                throw new IllegalStateException("Failed to enable log system.");
            }

            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now());

            int count = 0;
            for (File file : files) {
                if (!file.getName().startsWith(date)) {
                    continue;
                }
                count++;
            }

            File oldLog = new File(logs, date + "-" + count + ".log");
            if (oldLog.exists()) {
                FileInputStream input = new FileInputStream(oldLog);
                GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(new File(logs, date + "-" + count + ".log.gz")));
                byte[] buf = new byte[1024];
                int len;
                while ((len = input.read(buf)) > 0) {
                    output.write(buf, 0, len);
                }
                output.finish();
                output.close();
                input.close();
                oldLog.delete();
            }

            File log = new File(logs, date + "-" + (count + 1) + ".log");
            log.createNewFile();

            this.logOutput = new FileOutputStream(log, true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        this.lineSeparator = System.lineSeparator().getBytes();

        Async.THREAD_POOL.scheduleAtFixedRate(this, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public static long currentTick;
    public volatile boolean running = true;

    @Override
    public void run() {
        try {
            for (Module module : Module.MODULE_MAP.values()) {
                if (!module.getConfig().enabled) {
                    continue;
                }
                module.tickAsync(currentTick, module.getConfig());
            }

            long currTime = System.currentTimeMillis();
            Horizon.PLAYERS.values().forEach(player -> player.tick(currentTick, currTime));

            String time = "[" + DateUtils.now() + "] ";
            for (String message : Async.LOG) {
                this.logOutput.write((time + message).getBytes());
                this.logOutput.write(lineSeparator);
            }
            Async.LOG.clear();
            this.logOutput.flush();

            currentTick++;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}