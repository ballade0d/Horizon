package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class HitBoxData extends Data {

    // Hit
    public List<Pair<Location, Long>> history = new ArrayList<>();
    public float deltaYaw;
    public float deltaPitch;

    public Location getHistoryLocation(final long ping, final boolean lerp) {
        long time = System.currentTimeMillis();
        long rewindTime = ping + 175;
        for (int i = history.size() - 1; i >= 0; i--) {
            long elapsedTime = time - history.get(i).value;
            if (elapsedTime < rewindTime) {
                continue;
            }
            if (i == history.size() - 1) {
                return history.get(i).key;
            }
            if (lerp) {
                double nextMoveWeight = (elapsedTime - rewindTime) / (double) (elapsedTime - (time - history.get(i + 1).value));
                Location before = history.get(i).key;
                Location after = history.get(i + 1).key;
                Vector3D interpolate = after.toVector().subtract(before.toVector());
                interpolate.multiply(nextMoveWeight);
                before.add(interpolate);
                return before;
            } else {
                return history.get(i).key;
            }
        }
        return history.get(0).key;
    }

    public Vector3D getHistoryVelocity(final long ping) {
        if (history == null || history.size() == 0) {
            return new Vector3D(0, 0, 0);
        }
        long currentTime = System.currentTimeMillis();
        long rewindTime = ping + 175;
        for (int i = history.size() - 1; i >= 0; i--) {
            int elapsedTime = (int) (currentTime - history.get(i).value);
            if (elapsedTime < rewindTime) {
                continue;
            }
            if (i == history.size() - 1) {
                return new Vector3D(0, 0, 0);
            }
            Location before = history.get(i).key;
            Location after = history.get(i + 1).key;
            return after.toVector().subtract(before.toVector());
        }
        return new Vector3D(0, 0, 0);
    }
}