package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

public class Vec3D implements Cloneable {

    private static final double EPSILON = 0.000001;

    public double x;
    public double y;
    public double z;

    public Vec3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vec3D(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3D(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3D(final float x, final float y, final float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3D add(final Vec3D vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
        return this;
    }

    public Vec3D add(final org.bukkit.util.Vector vec) {
        x += vec.getX();
        y += vec.getY();
        z += vec.getZ();
        return this;
    }

    public Vec3D subtract(final Vec3D vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
        return this;
    }

    public Vec3D subtract(final org.bukkit.util.Vector vec) {
        x -= vec.getX();
        y -= vec.getY();
        z -= vec.getZ();
        return this;
    }

    public Vec3D multiply(final Vec3D vec) {
        x *= vec.x;
        y *= vec.y;
        z *= vec.z;
        return this;
    }

    public Vec3D divide(final Vec3D vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
        return this;
    }

    public Vec3D copy(final Vec3D vec) {
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

    public double distance(final Vec3D o) {
        return Math.sqrt(this.distanceSquared(o));
    }

    public double distanceSquared(final Vec3D o) {
        return NumberConversions.square(x - o.x) + NumberConversions.square(y - o.y) + NumberConversions.square(z - o.z);
    }

    public float angle(final Vec3D other) {
        double dot = Math.min(Math.max(dot(other) / (length() * other.length()), -1), 1);
        return (float) Math.acos(dot);
    }

    public Vec3D midpoint(final Vec3D other) {
        x = (x + other.x) / 2;
        y = (y + other.y) / 2;
        z = (z + other.z) / 2;
        return this;
    }

    public Vec3D getMidpoint(final Vec3D other) {
        double x = (this.x + other.x) / 2;
        double y = (this.y + other.y) / 2;
        double z = (this.z + other.z) / 2;
        return new Vec3D(x, y, z);
    }

    public Vec3D multiply(final int m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    public Vec3D multiply(final double m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    public Vec3D multiply(final float m) {
        x *= m;
        y *= m;
        z *= m;
        return this;
    }

    public double dot(final Vec3D other) {
        return x * other.x + y * other.y + z * other.z;
    }

    public Vec3D crossProduct(final Vec3D o) {
        double newX = y * o.z - o.y * z;
        double newY = z * o.x - o.z * x;
        double newZ = x * o.y - o.x * y;
        x = newX;
        y = newY;
        z = newZ;
        return this;
    }

    public Vec3D getCrossProduct(final Vec3D o) {
        double x = this.y * o.z - o.y * this.z;
        double y = this.z * o.x - o.z * this.x;
        double z = this.x * o.y - o.x * this.y;
        return new Vec3D(x, y, z);
    }

    public Vec3D normalize() {
        double length = length();
        x /= length;
        y /= length;
        z /= length;
        return this;
    }

    public Vec3D zero() {
        x = 0;
        y = 0;
        z = 0;
        return this;
    }

    public boolean isInAABB(final Vec3D min, final Vec3D max) {
        return x >= min.x && x <= max.x && y >= min.y && y <= max.y && z >= min.z && z <= max.z;
    }

    public boolean isInSphere(final Vec3D origin, final double radius) {
        return (NumberConversions.square(origin.x - x) + NumberConversions.square(origin.y - y) + NumberConversions.square(origin.z - z)) <= NumberConversions.square(radius);
    }

    public Vec3D setX(final int x) {
        this.x = x;
        return this;
    }

    public Vec3D setX(final double x) {
        this.x = x;
        return this;
    }

    public Vec3D setX(final float x) {
        this.x = x;
        return this;
    }

    public Vec3D setY(final int y) {
        this.y = y;
        return this;
    }

    public Vec3D setY(final double y) {
        this.y = y;
        return this;
    }

    public Vec3D setY(final float y) {
        this.y = y;
        return this;
    }

    public Vec3D setZ(final int z) {
        this.z = z;
        return this;
    }

    public Vec3D setZ(final double z) {
        this.z = z;
        return this;
    }

    public Vec3D setZ(final float z) {
        this.z = z;
        return this;
    }

    public Location toLocation(World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3D)) {
            return false;
        }
        Vec3D other = (Vec3D) obj;
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
    public Vec3D clone() {
        try {
            return (Vec3D) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}