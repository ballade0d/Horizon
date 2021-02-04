package xyz.hstudio.horizon.util;

import net.minecraft.server.v1_8_R3.MathHelper;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;

public class MathUtils {

    public static Vector3D getDirection(double yaw, double pitch) {
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        double xz = MathHelper.cos(rotY);

        double x = -xz * MathHelper.sin(rotX);
        double y = -MathHelper.sin(rotY);
        double z = xz * MathHelper.cos(rotX);
        return new Vector3D(x, y, z);
    }

    /**
     * Get greatest common divisor of two values
     * Please make sure the two values are positive
     *
     * @param a value a
     * @param b value b
     * @return the gcd
     */
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

    public static double stdev(double[] data) {
        double mean = mean(data);
        double dividend = 0;
        for (double num : data) {
            dividend += Math.pow(num - mean, 2);
        }
        return Math.sqrt(dividend / (data.length - 1));
    }

    public static double mean(double[] data) {
        double ans = 0;
        for (double num : data) {
            ans += num;
        }
        ans /= data.length;
        return ans;
    }

    public static double average(double[] data) {
        return Arrays.stream(data).average().orElse(0);
    }
}