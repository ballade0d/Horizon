package xyz.hstudio.horizon.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.IOUtils;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.wrapper.EntityBase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

public class Async implements Runnable {

    private final Horizon inst;
    private final ScheduledExecutorService threadPool;
    private final AtomicInteger tick = new AtomicInteger();

    public Async(Horizon inst) {
        this.inst = inst;
        threadPool = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Horizon Processing Thread")
                .build());

        try {
            File logs = new File(inst.getDataFolder(), "logs");
            if (!logs.exists() && !logs.mkdirs()) {
                throw new IllegalStateException("Failed to enable log system.");
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
                IOUtils.copy(input, output);
                output.close();
                input.close();
                if (!oldLog.delete()) {
                    throw new IllegalStateException("Failed to enable log system.");
                }
            }

            File log = new File(logs, date + "-" + (count + 1) + ".log");
            if (!log.createNewFile()) {
                throw new IllegalStateException("Failed to enable log system.");
            }

            this.logOutput = new FileOutputStream(log, true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void start() {
        threadPool.scheduleAtFixedRate(this, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        threadPool.shutdown();
    }

    public int getTick() {
        return tick.get();
    }

    @Override
    public void run() {
        executeChecks();
        trackEntities();
        writeLogs();

        tick.incrementAndGet();
    }

    /**
     * Check executor
     */

    private void executeChecks() {
        for (HPlayer p : inst.getPlayers().values()) {
            for (CheckBase check : p.checks.values()) {
                check.tickAsync(tick.get());
                check.decay(tick.get());
            }
        }
    }

    /**
     * Entity tracker
     */
    private final Map<EntityBase, List<Pair<Location, Integer>>> trackedEntities = new ConcurrentHashMap<>();

    private void trackEntities() {
        if (tick.get() % 20 == 0) {
            Set<EntityBase> collectedEntities = new HashSet<>();

            for (HPlayer p : inst.getPlayers().values()) {
                collectedEntities.add(p.base);
                collectedEntities.addAll(p.getWorld().getNearbyEntities(p.physics.position, 10, 10, 10));
            }

            for (EntityBase entity : collectedEntities) {
                trackedEntities.put(entity, trackedEntities.getOrDefault(entity, new ArrayList<>()));
            }

            Set<EntityBase> expiredEntities = new HashSet<>(trackedEntities.keySet());
            expiredEntities.removeAll(collectedEntities);

            for (EntityBase expired : expiredEntities) {
                trackedEntities.remove(expired);
            }
        } else {
            for (HPlayer p : inst.getPlayers().values()) {
                trackedEntities.put(p.base, trackedEntities.getOrDefault(p.base, new ArrayList<>()));
            }
        }


        for (EntityBase entity : trackedEntities.keySet()) {
            List<Pair<Location, Integer>> times = trackedEntities.getOrDefault(entity, new ArrayList<>());
            times.add(new Pair<>(entity.position(), tick.get()));
            if (times.size() > 30) {
                times.remove(0);
            }
            trackedEntities.put(entity, times);
        }
    }

    public List<Location> getHistory(EntityBase entity, int ping, int range) {
        List<Pair<Location, Integer>> times = trackedEntities.get(entity);
        if (times == null || times.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> history = new ArrayList<>();
        int ticks = tick.get() - NumberConversions.floor(ping / 50D);
        // Loop backwards
        for (int i = times.size() - 1; i >= 0; i--) {
            if (Math.abs(ticks - times.get(i).getValue()) > range) {
                continue;
            }
            history.add(times.get(i).getKey());
        }
        return history;
    }

    public void clearHistory(EntityBase entity) {
        trackedEntities.remove(entity);
    }

    /**
     * Log system
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Deque<String> logs = new ConcurrentLinkedDeque<>();
    private final FileOutputStream logOutput;

    private void writeLogs() {
        String time = "[" + FORMATTER.format(LocalDateTime.now()) + "] ";
        try {
            for (String message : logs) {
                logOutput.write((time + message).getBytes());
                logOutput.write(System.lineSeparator().getBytes());
            }
            logs.clear();
            logOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        logs.addLast(message);
    }
}