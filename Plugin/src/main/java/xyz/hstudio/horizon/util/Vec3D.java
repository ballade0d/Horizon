package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.util.NumberConversions;

@AllArgsConstructor
@NoArgsConstructor
public class Vec3D implements Cloneable {

    private static final double epsilon = 0.000001;

    @Getter
    @Setter
    protected double x, y, z;

    public Vec3D add(Vec3D vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public Vec3D subtract(Vec3D vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public Vec3D multiply(Vec3D vec) {
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }

    public Vec3D divide(Vec3D vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
        return this;
    }

    public Vec3D multiply(double m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z);
    }

    public double distance(Vec3D vec) {
        return Math.sqrt(distanceSquared(vec));
    }

    public double distanceSquared(Vec3D vec) {
        return NumberConversions.square(x - vec.x) + NumberConversions.square(y - vec.y) + NumberConversions.square(z - vec.z);
    }

    public double dot(Vec3D vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public double angle(Vec3D vec) {
        double dot = Math.min(Math.max(dot(vec) / (length() * vec.length()), -1), 1);
        return Math.acos(dot);
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vec3D other = (Vec3D) obj;
        return Math.abs(x - other.x) < epsilon && Math.abs(y - other.y) < epsilon && Math.abs(z - other.z) < epsilon;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (Double.doubleToLongBits(x) ^ Double.doubleToLongBits(x) >>> 32);
        hash = 79 * hash + (int) (Double.doubleToLongBits(y) ^ Double.doubleToLongBits(y) >>> 32);
        hash = 79 * hash + (int) (Double.doubleToLongBits(z) ^ Double.doubleToLongBits(z) >>> 32);
        return hash;
    }

    @Override
    public Vec3D clone() {
        try {
            return (Vec3D) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}