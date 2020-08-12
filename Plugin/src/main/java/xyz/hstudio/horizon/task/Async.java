package xyz.hstudio.horizon.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.module.ModuleBase;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vec3D;
import xyz.hstudio.horizon.wrapper.EntityBase;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
public class Async implements Runnable {

    private final Horizon horizon;
    private final Map<EntityBase, List<Pair<Location, Long>>> trackedEntities = new ConcurrentHashMap<>();
    @Getter
    private long tick;

    @Override
    public void run() {
        if (tick % 20 == 0) {
            Set<EntityBase> collectedEntities = new HashSet<>();

            for (HPlayer player : horizon.getPlayers().values()) {
                List<EntityBase> nearbyEntities = player.getWorld().getNearbyEntities(player.position, 40, 20, 40);
                for (EntityBase entity : nearbyEntities) {
                    if (entity instanceof LivingEntity || entity instanceof Vehicle || entity instanceof Fireball) {
                        collectedEntities.add(entity);
                    }
                }
            }

            for (EntityBase entity : collectedEntities) {
                trackedEntities.put(entity, trackedEntities.getOrDefault(entity, new CopyOnWriteArrayList<>()));
            }

            Set<EntityBase> expiredEntities = new HashSet<>(trackedEntities.keySet());
            expiredEntities.removeAll(collectedEntities);

            for (EntityBase expired : expiredEntities) {
                trackedEntities.remove(expired);
            }
        }
        for (HPlayer player : horizon.getPlayers().values()) {
            trackedEntities.put(player.getBase(), trackedEntities.getOrDefault(player.getBase(), new CopyOnWriteArrayList<>()));
        }
        for (EntityBase entity : trackedEntities.keySet()) {
            List<Pair<Location, Long>> times = trackedEntities.getOrDefault(entity, new CopyOnWriteArrayList<>());
            long currTime = System.currentTimeMillis();
            times.add(new Pair<>(entity.getPosition(), currTime));
            if (times.size() > 20) {
                times.remove(0);
            }
            trackedEntities.put(entity, times);
        }

        for (ModuleBase moduleBase : ModuleBase.MODULES.values()) {
            moduleBase.decay(tick);
        }
        tick++;
    }

    public Location getHistoryLocation(int ping, EntityBase entity) {
        List<Pair<Location, Long>> times = trackedEntities.get(entity);
        if (times == null || times.size() == 0) {
            return entity.getPosition();
        }
        long currentTime = System.currentTimeMillis();
        int rewindTime = ping + 175;
        for (int i = times.size() - 1; i >= 0; i--) {
            int elapsedTime = (int) (currentTime - times.get(i).getValue());
            if (elapsedTime >= rewindTime) {
                if (i == times.size() - 1) {
                    return times.get(i).getKey();
                }
                double nextMoveWeight = (elapsedTime - rewindTime) / (double) (elapsedTime - (currentTime - times.get(i + 1).getValue()));
                Location before = times.get(i).getKey().clone();
                Location after = times.get(i + 1).getKey().clone();
                Vec3D interpolate = after.subtract(before);
                interpolate.multiply(nextMoveWeight);
                before.add(interpolate);
                return before;
            }
        }
        return times.get(0).getKey().clone();
    }
}