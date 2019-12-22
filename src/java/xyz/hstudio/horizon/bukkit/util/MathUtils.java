package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.McAccess;

public class MathUtils {

    public static double round(double number, final int decimals) {
        number *= Math.pow(10, decimals);
        number = Math.round(number);
        return number / Math.pow(10, decimals);
    }

    public static Vector getDirection(final float yaw, final float pitch) {
        Vector vector = new Vector();
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        vector.setY(-McAccess.getInst().sin(rotY));
        double xz = McAccess.getInst().cos(rotY);
        vector.setX(-xz * McAccess.getInst().sin(rotX));
        vector.setZ(xz * McAccess.getInst().cos(rotX));
        return vector;
    }

    public static double angle(final Vector a, final Vector b) {
        double dot = Math.min(Math.max(a.dot(b) / (a.length() * b.length()), -1), 1);
        return Math.acos(dot);
    }
}