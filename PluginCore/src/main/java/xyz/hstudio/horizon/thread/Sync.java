package xyz.hstudio.horizon.thread;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.wrap.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Sync implements Runnable {

    private static final Map<HoriPlayer, Location> pendingTeleports = new ConcurrentHashMap<>();

    public static void teleport(final HoriPlayer player, final Location to) {
        pendingTeleports.put(player, to);
    }

    @Override
    public void run() {
        for (Map.Entry<HoriPlayer, Location> entry : pendingTeleports.entrySet()) {
            entry.getKey().teleport(entry.getValue());
        }
        pendingTeleports.clear();
    }
}