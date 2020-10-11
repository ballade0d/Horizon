package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.NumberConversions;

@AllArgsConstructor
public class Vector3D {

    @Getter
    protected final double x, y, z;

    public Vector3D plus(Vector3D vec) {
        return new Vector3D(x + vec.x, y + vec.y, z + vec.z);
    }

    public Vector3D plus(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D minus(Vector3D vec) {
        return new Vector3D(x - vec.x, y - vec.y, z - vec.z);
    }

    public Vector3D minus(double x, double y, double z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    public Vector3D multiply(Vector3D vec) {
        return new Vector3D(x * vec.x, y * vec.y, z * vec.z);
    }

    public Vector3D divide(Vector3D vec) {
        return new Vector3D(x / vec.x, y / vec.y, z / vec.z);
    }

    public Vector3D multiply(double m) {
        return new Vector3D(x * m, y * m, z * m);
    }

    public Vector3D setX(double x) {
        return new Vector3D(x, y, z);
    }

    public Vector3D setY(double y) {
        return new Vector3D(x, y, z);
    }

    public Vector3D setZ(double z) {
        return new Vector3D(x, y, z);
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

    public double distance2d(Vector3D vec) {
        return Math.sqrt(distance2dSquared(vec));
    }

    public double distance2dSquared(Vector3D vec) {
        return NumberConversions.square(x - vec.x) + NumberConversions.square(z - vec.z);
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

    public AABB toAABB(double length, double width) {
        width /= 2;
        return new AABB(x - width, y, z - width, x + width, y + length, z + width);
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
}