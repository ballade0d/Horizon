package xyz.hstudio.horizon.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.wrapper.EntityBase;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Async implements Runnable {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    private final ScheduledExecutorService threadPool;
    private final Map<EntityBase, List<Pair<Location, Integer>>> trackedEntities;
    @Getter
    private int tick;

    public Async() {
        threadPool = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Horizon Processing Thread")
                .build());

        trackedEntities = new ConcurrentHashMap<>();
    }

    public void start() {
        threadPool.scheduleAtFixedRate(this, 50L, 50L, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        threadPool.shutdown();
    }

    @Override
    public void run() {
        if (tick % 20 == 0) {
            Set<EntityBase> collectedEntities = new HashSet<>();

            for (HPlayer p : inst.getPlayers().values()) {
                for (EntityBase entity : p.getWorld().getNearbyEntities(p.getPhysics().getPosition(), 10, 10, 10)) {
                    Entity bkEntity = entity.bukkit();
                    if (bkEntity instanceof LivingEntity || bkEntity instanceof Vehicle || bkEntity instanceof Fireball) {
                        collectedEntities.add(entity);
                    }
                }
                collectedEntities.add(p.base());
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
                trackedEntities.put(p.base(), trackedEntities.getOrDefault(p.base(), new ArrayList<>()));
            }
        }

        for (EntityBase entity : trackedEntities.keySet()) {
            List<Pair<Location, Integer>> times = trackedEntities.getOrDefault(entity, new ArrayList<>());
            times.add(new Pair<>(entity.position(), tick));
            if (times.size() > 30) {
                times.remove(0);
            }
            trackedEntities.put(entity, times);
        }

        tick++;
    }

    public List<Location> getHistory(EntityBase entity, int ping, int range) {
        List<Pair<Location, Integer>> times = trackedEntities.get(entity);
        if (times == null || times.size() == 0) {
            return Collections.emptyList();
        }
        List<Location> locations = new ArrayList<>();
        int ticks = tick - NumberConversions.floor(ping / 50D);
        for (int i = times.size() - 1; i >= 0; i--) {
            if (Math.abs(ticks - times.get(i).getValue()) > range) {
                continue;
            }
            locations.add(times.get(i).getKey());
        }
        return locations;
    }
}