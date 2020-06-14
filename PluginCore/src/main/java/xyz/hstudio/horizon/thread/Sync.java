package xyz.hstudio.horizon.thread;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Sync implements Runnable {

    private static final Map<HoriPlayer, Location> pendingTeleports = new ConcurrentHashMap<>();
    private static final Map<Entity, List<Pair<Location, Long>>> trackedEntities = new ConcurrentHashMap<>();

    public static void teleport(final HoriPlayer player, final Location to) {
        if (player == null || to == null) {
            return;
        }
        pendingTeleports.put(player, to);
    }

    @Override
    public void run() {
        for (Map.Entry<HoriPlayer, Location> entry : pendingTeleports.entrySet()) {
            entry.getKey().teleport(entry.getValue());
        }
        pendingTeleports.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            List<Pair<Location, Long>> times = trackedEntities.getOrDefault(p, new CopyOnWriteArrayList<>());
            long currTime = System.currentTimeMillis();
            times.add(new Pair<>(new Location(p.getLocation()), currTime));
            if (times.size() > 20) {
                times.remove(0);
            }
            trackedEntities.put(p, times);
        }
    }

    public static Location getHistoryLocation(final int time, final Entity entity) {
        List<Pair<Location, Long>> times = trackedEntities.get(entity);
        if (times == null || times.size() == 0) {
            return new Location(entity.getLocation());
        }
        long currentTime = System.currentTimeMillis();
        int rewindTime = time + 175;
        for (int i = times.size() - 1; i >= 0; i--) {
            int elapsedTime = (int) (currentTime - times.get(i).value);
            if (elapsedTime >= rewindTime) {
                if (i == times.size() - 1) {
                    return times.get(i).key;
                }
                double nextMoveWeight = (elapsedTime - rewindTime) / (double) (elapsedTime - (currentTime - times.get(i + 1).value));
                Location before = times.get(i).key;
                Location after = times.get(i + 1).key;
                Vector3D interpolate = after.toVector().subtract(before.toVector());
                interpolate.multiply(nextMoveWeight);
                before.add(interpolate);
                return before;
            }
        }
        return times.get(0).key;
    }
}