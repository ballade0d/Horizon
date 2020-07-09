package xyz.hstudio.horizon.util;

import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.List;

public final class MathUtils {

    private MathUtils() {
    }

    // Changed Math.sin and Math.cos to NMS's for better performance.
    public static Vector3D getDirection(final float yaw, final float pitch) {
        Vector3D vector = new Vector3D();
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        vector.setY(-McAccessor.INSTANCE.sin(rotY));
        double xz = McAccessor.INSTANCE.cos(rotY);
        vector.setX(-xz * McAccessor.INSTANCE.sin(rotX));
        vector.setZ(xz * McAccessor.INSTANCE.cos(rotX));
        return vector;
    }

    public static double angle(final Vector3D a, final Vector3D b) {
        double dot = Math.min(Math.max(a.dot(b) / (a.length() * b.length()), -1), 1);
        return Math.acos(dot);
    }

    public static double distance2d(final double xDiff, final double zDiff) {
        return Math.sqrt(xDiff * xDiff + zDiff * zDiff);
    }

    public static double getStandardDeviation(final List<Double> doubles) {
        double sum = 0, deviation = 0;
        int length = doubles.size();
        for (double num : doubles) {
            sum += num;
        }
        double mean = sum / length;
        for (double num : doubles) {
            deviation += NumberConversions.square(num - mean);
        }
        return Math.sqrt(deviation / length);
    }
}