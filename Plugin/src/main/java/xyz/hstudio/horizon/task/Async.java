package xyz.hstudio.horizon.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class Async implements Runnable {

    private final Horizon horizon;
    private final Map<EntityBase, List<Pair<Location, Integer>>> trackedEntities = new ConcurrentHashMap<>();
    @Getter
    private int tick;

    @Override
    public void run() {
        if (tick % 20 == 0) {
            Set<EntityBase> collectedEntities = new HashSet<>();

            for (HPlayer p : horizon.getPlayers().values()) {
                for (EntityBase entity : p.getWorld().getNearbyEntities(p.getPhysics().getPos(), 10, 10, 10)) {
                    Entity bkEntity = entity.getBukkitEntity();
                    if (bkEntity instanceof LivingEntity || bkEntity instanceof Vehicle || bkEntity instanceof Fireball) {
                        collectedEntities.add(entity);
                    }
                }
                collectedEntities.add(p.getBase());
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
            for (HPlayer p : horizon.getPlayers().values()) {
                trackedEntities.put(p.getBase(), trackedEntities.getOrDefault(p.getBase(), new ArrayList<>()));
            }
        }

        for (EntityBase entity : trackedEntities.keySet()) {
            List<Pair<Location, Integer>> times = trackedEntities.getOrDefault(entity, new ArrayList<>());
            times.add(new Pair<>(entity.getPosition(), tick));
            if (times.size() > 30) {
                times.remove(0);
            }
            trackedEntities.put(entity, times);
        }

        tick++;
    }

    public List<Location> getHistoryLocation(EntityBase entity, int ping) {
        List<Pair<Location, Integer>> times = trackedEntities.get(entity);
        if (times == null || times.size() == 0) {
            return Collections.emptyList();
        }
        List<Location> locations = new ArrayList<>();
        int rewindTicks = NumberConversions.floor(ping / 50D);
        for (int i = times.size() - 1; i >= 0; i--) {
            if (Math.abs(tick - times.get(i).getValue() - rewindTicks) > 1) {
                continue;
            }
            locations.add(times.get(i).getKey());
        }
        return locations;
    }
}