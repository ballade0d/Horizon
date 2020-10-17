package xyz.hstudio.horizon.util;

import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.wrapper.AccessorBase;

public class MathUtils {

    public static Vector3D getDirection(float yaw, float pitch) {
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        double xz = AccessorBase.getInst().cos(rotY);

        double x = -xz * AccessorBase.getInst().sin(rotX);
        double y = -AccessorBase.getInst().sin(rotY);
        double z = xz * AccessorBase.getInst().cos(rotX);
        return new Vector3D(x, y, z);
    }

    public static float gcd(float a, float b) {
        if (a < b) {
            return gcd(b, a);
        }
        if (Math.abs(b) < 0.001) {
            return a;
        } else {
            return gcd(b, a - NumberConversions.floor(a / b) * b);
        }
    }

    public static float lcm(float a, float b) {
        return (a / gcd(a, b)) * b;
    }
}