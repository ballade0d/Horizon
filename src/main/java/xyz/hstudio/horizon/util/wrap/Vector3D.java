package xyz.hstudio.horizon.util.wrap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.util.MathUtils;

public class Vector3D implements Cloneable {

    private static final double EPSILON = 0.000001;

    public double x;
    public double y;
    public double z;

    public Vector3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3D(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D add(final Vector3D vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public Vector3D add(final org.bukkit.util.Vector vec) {
        x += vec.getX();
        y += vec.getY();
        z += vec.getZ();
        return this;
    }

    public Vector3D subtract(final Vector3D vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public Vector3D subtract(final org.bukkit.util.Vector vec) {
        x -= vec.getX();
        y -= vec.getY();
        z -= vec.getZ();
        return this;
    }

    public Vector3D multiply(final Vector3D vec) {
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }

    public Vector3D divide(final Vector3D vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
        return this;
    }

    public Vector3D copy(final Vector3D vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
        return this;
    }

    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    public double lengthSquared() {
        return NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z);
    }

    public double distance(final Vector3D o) {
        return Math.sqrt(this.distanceSquared(o));
    }

    public double distanceSquared(final Vector3D o) {
        return NumberConversions.square(x - o.x) + NumberConversions.square(y - o.y) + NumberConversions.square(z - o.z);
    }

    public float angle(final Vector3D other) {
        double dot = Math.min(Math.max(dot(other) / (length() * other.length()), -1), 1);
        return (float) Math.acos(dot);
    }

    public Vector3D midpoint(final Vector3D other) {
        x = (x + other.x) / 2;
        y = (y + other.y) / 2;
        z = (z + other.z) / 2;
        return this;
    }

    public Vector3D getMidpoint(final Vector3D other) {
        double x = (this.x + other.x) / 2;
        double y = (this.y + other.y) / 2;
        double z = (this.z + other.z) / 2;
        return new Vector3D(x, y, z);
    }

    public Vector3D multiply(final int m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    public Vector3D multiply(final double m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    public Vector3D multiply(final float m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    public double dot(final Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vector3D crossProduct(final Vector3D o) {
        double newX = y * o.z - o.y * z;
        double newY = z * o.x - o.z * x;
        double newZ = x * o.y - o.x * y;
        x = newX;
        y = newY;
        z = newZ;
        return this;
    }

    public Vector3D getCrossProduct(final Vector3D o) {
        double x = this.y * o.z - o.y * this.z;
        double y = this.z * o.x - o.z * this.x;
        double z = this.x * o.y - o.x * this.y;
        return new Vector3D(x, y, z);
    }

    public Vector3D normalize() {
        double length = length();
        x /= length;
        y /= length;
        z /= length;
        return this;
    }

    public Vector3D zero() {
        x = 0;
        y = 0;
        z = 0;
        return this;
    }

    public boolean isInAABB(final Vector3D min, final Vector3D max) {
        return x >= min.x && x <= max.x && y >= min.y && y <= max.y && z >= min.z && z <= max.z;
    }

    public boolean isInSphere(final Vector3D origin, final double radius) {
        return (NumberConversions.square(origin.x - x) + NumberConversions.square(origin.y - y) + NumberConversions.square(origin.z - z)) <= NumberConversions.square(radius);
    }

    public Vector3D setX(final int x) {
        this.x = x;
        return this;
    }

    public Vector3D setX(final double x) {
        this.x = x;
        return this;
    }

    public Vector3D setX(final float x) {
        this.x = x;
        return this;
    }

    public Vector3D setY(final int y) {
        this.y = y;
        return this;
    }

    public Vector3D setY(final double y) {
        this.y = y;
        return this;
    }

    public Vector3D setY(final float y) {
        this.y = y;
        return this;
    }

    public Vector3D setZ(final int z) {
        this.z = z;
        return this;
    }

    public Vector3D setZ(final double z) {
        this.z = z;
        return this;
    }

    public Vector3D setZ(final float z) {
        this.z = z;
        return this;
    }

    public Location toLocation(World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vector3D)) {
            return false;
        }
        Vector3D other = (Vector3D) obj;
        return MathUtils.abs(x - other.x) < EPSILON && MathUtils.abs(y - other.y) < EPSILON && MathUtils.abs(z - other.z) < EPSILON && (this.getClass().equals(obj.getClass()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public Vector3D clone() {
        try {
            return (Vector3D) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}