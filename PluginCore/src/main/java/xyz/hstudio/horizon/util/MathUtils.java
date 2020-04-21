package xyz.hstudio.horizon.util;

import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.Arrays;

public final class MathUtils {

    private MathUtils() {
    }

    // Skidded from Bukkit lol.
    // But I switched Math.sin and Math.cos to NMS's for better performance.
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

    public static double average(final Double[] data) {
        return Arrays.stream(data).mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public static double standardDeviation(final Double[] data) {
        double average = average(data);
        double total = 0;
        for (double num : data) {
            total += NumberConversions.square(num - average);
        }
        return Math.sqrt(total / data.length);
    }

    public static double distance2d(final double xDiff, final double zDiff) {
        return Math.sqrt(xDiff * xDiff + zDiff * zDiff);
    }

    public static double distance3d(final double xDiff, final double yDiff, final double zDiff) {
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }
}