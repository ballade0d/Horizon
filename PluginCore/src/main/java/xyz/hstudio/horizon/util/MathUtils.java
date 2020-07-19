package xyz.hstudio.horizon.util;

import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * Get the pair of the high and low outliers of numbers
     */
    public static Pair<List<Double>, List<Double>> getOutliers(final Collection<? extends Number> collection) {
        List<Double> values = new ArrayList<>();

        for (Number number : collection) {
            values.add(number.doubleValue());
        }

        double q1 = getMedian(values.subList(0, values.size() / 2));
        double q3 = getMedian(values.subList(values.size() / 2, values.size()));

        double iqr = Math.abs(q1 - q3);
        double lowThreshold = q1 - 1.5 * iqr, highThreshold = q3 + 1.5 * iqr;

        Pair<List<Double>, List<Double>> tuple = new Pair<>(new ArrayList<>(), new ArrayList<>());

        for (Double value : values) {
            if (value < lowThreshold) {
                tuple.key.add(value);
            } else if (value > highThreshold) {
                tuple.value.add(value);
            }
        }

        return tuple;
    }

    /**
     * Get the middle number of that data
     */
    public static double getMedian(final List<Double> data) {
        if (data.size() % 2 == 0) {
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        } else {
            return data.get(data.size() / 2);
        }
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