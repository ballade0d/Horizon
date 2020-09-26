package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.util.NumberConversions;

@AllArgsConstructor
@NoArgsConstructor
public class Vector3D implements Cloneable {

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
        if (!(obj instanceof Vector3D)) {
            return false;
        }
        Vector3D other = (Vector3D) obj;
        if (Double.doubleToRawLongBits(x) != Double.doubleToRawLongBits(other.x)) {
            return false;
        } else if (Double.doubleToRawLongBits(y) != Double.doubleToRawLongBits(other.y)) {
            return false;
        } else return Double.doubleToRawLongBits(z) == Double.doubleToRawLongBits(other.z);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
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