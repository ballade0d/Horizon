package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.IMcAccessor;
import xyz.hstudio.horizon.bukkit.compat.McAccessor;

public class MathUtils {

    private static final int MASK_NON_SIGN_INT = 0x7fffffff;
    private static final long MASK_NON_SIGN_LONG = 0x7fffffffffffffffL;

    // Skidded from Bukkit lol.
    // But I switched Math.sin and Math.cos to NMS's for better performance.
    public static Vector getDirection(final float yaw, final float pitch) {
        Vector vector = new Vector();
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        vector.setY(-McAccessor.INSTANCE.sin(rotY));
        double xz = McAccessor.INSTANCE.cos(rotY);
        vector.setX(-xz * McAccessor.INSTANCE.sin(rotX));
        vector.setZ(xz * McAccessor.INSTANCE.cos(rotX));
        return vector;
    }

    public static double angle(final Vector a, final Vector b) {
        double dot = Math.min(Math.max(a.dot(b) / (a.length() * b.length()), -1), 1);
        return Math.acos(dot);
    }

    // Faster than Math.abs
    public static int abs(final int v) {
        int i = v >>> 31;
        return (v ^ (~i + 1)) + i;
    }

    // Faster than Math.abs
    public static long abs(final long v) {
        long l = v >>> 63;
        return (v ^ (~l + 1)) + l;
    }

    // Faster than Math.abs
    public static float abs(final float v) {
        return Float.intBitsToFloat(MASK_NON_SIGN_INT & Float.floatToRawIntBits(v));
    }

    // Faster than Math.abs
    public static double abs(final double v) {
        return Double.longBitsToDouble(MASK_NON_SIGN_LONG & Double.doubleToRawLongBits(v));
    }
}