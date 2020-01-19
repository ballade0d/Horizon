package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.McAccess;

import java.util.List;

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

    public boolean isOnGround(final AABB cube, final boolean ignoreOnGround) {
        AABB surrounding = new AABB(cube.minX, cube.minY - 1, cube.minZ, cube.maxX, cube.minY, cube.maxZ);
        AABB underFeet = new AABB(cube.minX, cube.minY - 0.001, cube.minZ, cube.maxX, cube.minY, cube.maxZ);
        AABB topFeet = cube.add(0, 0.001, 0, 0, 0, 0);
        List<Block> above = !ignoreOnGround ? null : topFeet.getBlocks(this.world);
        for (Block block : surrounding.getBlocks(this.world)) {
            if (block.isLiquid() || !block.getType().isSolid()) {
                continue;
            }
            // Ignore if player is in ground
            for (AABB bBox : McAccess.getInst().getBoxes(block)) {
                if (!bBox.isColliding(underFeet)) {
                    continue;
                }
                if (ignoreOnGround) {
                    for (Block aboveBlock : above) {
                        if (aboveBlock.isLiquid() || !aboveBlock.getType().isSolid()) {
                            continue;
                        }
                        for (AABB aboveBox : McAccess.getInst().getBoxes(aboveBlock)) {
                            if (aboveBox.isColliding(topFeet)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
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

    public double lengthSquared() {
        return NumberConversions.square(this.x) + NumberConversions.square(this.y) + NumberConversions.square(this.z);
    }

    public double distance(final Location loc) {
        return Math.sqrt(this.distanceSquared(loc));
    }

    public double distanceSquared(final Location loc) {
        return NumberConversions.square(this.x - loc.x) + NumberConversions.square(this.y - loc.y) + NumberConversions.square(this.z - loc.z);
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