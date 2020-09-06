package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.util.NumberConversions;

@AllArgsConstructor
@NoArgsConstructor
public class Vector3D implements Cloneable {

    private static final double epsilon = 0.000001;

    @Getter
    @Setter
    protected double x, y, z;

    public Vector3D add(Vector3D vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public Vector3D subtract(Vector3D vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public Vector3D multiply(Vector3D vec) {
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }

    public Vector3D divide(Vector3D vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
        return this;
    }

    public Vector3D multiply(double m) {
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

    public double distance(Vector3D vec) {
        return Math.sqrt(distanceSquared(vec));
    }

    public double distanceSquared(Vector3D vec) {
        return NumberConversions.square(x - vec.x) + NumberConversions.square(y - vec.y) + NumberConversions.square(z - vec.z);
    }

    public double dot(Vector3D vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public double angle(Vector3D vec) {
        double dot = Math.min(Math.max(dot(vec) / (length() * vec.length()), -1), 1);
        return Math.acos(dot);
    }

    public int getBlockX() {
        return NumberConversions.floor(x);
    }

    public int getBlockY() {
        return NumberConversions.floor(y);
    }

    public int getBlockZ() {
        return NumberConversions.floor(z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vector3D other = (Vector3D) obj;
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
    public Vector3D clone() {
        try {
            return (Vector3D) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}