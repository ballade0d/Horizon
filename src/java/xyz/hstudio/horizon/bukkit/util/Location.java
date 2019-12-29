package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

/**
 * A wrapped Location class. Remember to use this instead of using bukkit's.
 * This includes optimized math utils and async block-getter method.
 */
public class Location {

    public final World world;
    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public Location(final org.bukkit.Location loc) {
        this.world = loc.getWorld();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    public Location(final World world, final double x, final double y, final double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = 0;
        this.pitch = 0;
    }

    public Location(final World world, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Block getBlock() {
        return BlockUtils.getBlock(this);
    }

    public Block getBlockUnsafe() {
        return this.world.getBlockAt(this.getBlockX(), this.getBlockY(), this.getBlockZ());
    }

    public Location add(final double x, final double y, final double z) {
        double newX = this.x + x;
        double newY = this.y + y;
        double newZ = this.z + z;
        return new Location(this.world, newX, newY, newZ, yaw, pitch);
    }

    public Location add(final Vector vec) {
        double newX = this.x + vec.getX();
        double newY = this.y + vec.getY();
        double newZ = this.z + vec.getZ();
        return new Location(this.world, newX, newY, newZ, yaw, pitch);
    }

    public Location add(final Location loc) {
        double newX = this.x + loc.x;
        double newY = this.y + loc.y;
        double newZ = this.z + loc.z;
        return new Location(this.world, newX, newY, newZ, yaw, pitch);
    }

    public Vector getDirection() {
        return MathUtils.getDirection(this.yaw, this.pitch);
    }

    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    // Math.pow(x, 2) is faster than x*x ?
    public double lengthSquared() {
        return Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2);
    }

    public double distance(final Location loc) {
        return Math.sqrt(this.distanceSquared(loc));
    }

    // Math.pow(x, 2) is faster than x*x ?
    public double distanceSquared(final Location loc) {
        return Math.pow(this.x - loc.x, 2) + Math.pow(this.y - loc.y, 2) + Math.pow(this.z - loc.z, 2);
    }

    public Vector toVector() {
        return new Vector(this.x, this.y, this.z);
    }

    public int getBlockX() {
        return NumberConversions.floor(this.x);
    }

    public int getBlockY() {
        return NumberConversions.floor(this.y);
    }

    public int getBlockZ() {
        return NumberConversions.floor(this.z);
    }
}