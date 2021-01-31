package xyz.hstudio.horizon.util;

import org.bukkit.util.NumberConversions;

public class Vector3D {

    public double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Plus

    public Vector3D plus(double x, double y, double z) {
        return new Vector3D(this.x + x, this.y + y, this.z + z);
    }

    public Vector3D plus(Vector3D vec) {
        return plus(vec.x, vec.y, vec.z);
    }

    // Add

    public Vector3D add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3D add(Vector3D vec) {
        return add(vec.x, vec.y, vec.z);
    }

    // Minus

    public Vector3D minus(double x, double y, double z) {
        return new Vector3D(this.x - x, this.y - y, this.z - z);
    }

    public Vector3D minus(Vector3D vec) {
        return minus(vec.x, vec.y, vec.z);
    }

    // Subtract

    public Vector3D subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3D subtract(Vector3D vec) {
        return subtract(vec.x, vec.y, vec.z);
    }

    // Multiply

    public Vector3D multiply(double v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        return this;
    }

    // Divide

    public Vector3D divide(double v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        return this;
    }

    // Length

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public double lengthSquared() {
        return NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z);
    }

    // Distance 3d

    public double distance(Vector3D vec) {
        return Math.sqrt(distanceSquared(vec));
    }

    public double distanceSquared(Vector3D vec) {
        return NumberConversions.square(x - vec.x) + NumberConversions.square(y - vec.y) + NumberConversions.square(z - vec.z);
    }

    // Distance 2d

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

    public Vector3D normalize() {
        double length = this.length();
        this.x /= length;
        this.y /= length;
        this.z /= length;
        return this;
    }

    public Vector3D newX(double x) {
        return new Vector3D(x, y, z);
    }

    public Vector3D newY(double y) {
        return new Vector3D(x, y, z);
    }

    public Vector3D newZ(double z) {
        return new Vector3D(x, y, z);
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

    public AABB toAABB() {
        return new AABB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3);
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