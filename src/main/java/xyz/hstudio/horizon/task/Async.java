package xyz.hstudio.horizon.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.cgoo.api.util.ObjIntPair;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.wrapper.EntityWrapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final Map<EntityWrapper, List<ObjIntPair<Location>>> trackedEntities = new ConcurrentHashMap<>();

    private void trackEntities() {
        if (tick.get() % 20 == 0) {
            Set<EntityWrapper> collectedEntities = new HashSet<>();

            for (HPlayer p : inst.getPlayers().values()) {
                collectedEntities.add(p.base);
                collectedEntities.addAll(p.world().getNearbyEntities(p.physics.position, 10, 10, 10));
            }

            for (EntityWrapper entity : collectedEntities) {
                trackedEntities.put(entity, trackedEntities.getOrDefault(entity, new ArrayList<>()));
            }

            Set<EntityWrapper> expiredEntities = new HashSet<>(trackedEntities.keySet());
            expiredEntities.removeAll(collectedEntities);

            for (EntityWrapper expired : expiredEntities) {
                trackedEntities.remove(expired);
            }
        } else {
            for (HPlayer p : inst.getPlayers().values()) {
                trackedEntities.put(p.base, trackedEntities.getOrDefault(p.base, new ArrayList<>()));
            }
        }

        for (EntityWrapper entity : trackedEntities.keySet()) {
            List<ObjIntPair<Location>> times = trackedEntities.getOrDefault(entity, new ArrayList<>());
            times.add(new ObjIntPair<>(entity.position(), tick.get()));
            if (times.size() > 25) {
                times.remove(0);
            }
            trackedEntities.put(entity, times);
        }
    }

    public List<Location> getHistory(EntityWrapper entity, int ping, int range) {
        List<ObjIntPair<Location>> times = trackedEntities.get(entity);
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

    public void clearHistory(EntityWrapper entity) {
        trackedEntities.remove(entity);
    }
}