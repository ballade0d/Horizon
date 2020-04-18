package xyz.hstudio.horizon.util.wrap;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A wrapped Location class. Remember to use this instead of using bukkit's.
 * This includes optimized math utils and async block-getter method.
 */
public class Location implements Cloneable {

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

    public boolean isOnGround(final HoriPlayer player, final boolean ignoreOnGround, final double feetDepth) {
        List<Block> blocks = new ArrayList<>();
        blocks.addAll(BlockUtils.getBlocksInLocation(this));
        blocks.addAll(BlockUtils.getBlocksInLocation(this.add(0, -1, 0)));
        AABB underFeet = new AABB(this.x - 0.3, this.y - feetDepth, this.z - 0.3, this.x + 0.3, this.y, this.z + 0.3);
        AABB topFeet = underFeet.add(0, feetDepth + 0.00001, 0, 0, 0, 0);
        List<Block> aboveBlocks = !ignoreOnGround ? null : BlockUtils.getBlocksInLocation(this);
        for (Block block : blocks) {
            if (block.isLiquid() || !BlockUtils.isSolid(block)) {
                continue;
            }
            for (AABB bBox : McAccessor.INSTANCE.getBoxes(player, block)) {
                if (!bBox.isColliding(underFeet)) {
                    continue;
                }
                if (ignoreOnGround) {
                    for (Block above : aboveBlocks) {
                        if (above.isLiquid() || !BlockUtils.isSolid(above)) {
                            continue;
                        }
                        for (AABB aboveBox : McAccessor.INSTANCE.getBoxes(player, above)) {
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

    public Location add(final Vector3D vec) {
        double newX = this.x + vec.x;
        double newY = this.y + vec.y;
        double newZ = this.z + vec.z;
        return new Location(this.world, newX, newY, newZ, yaw, pitch);
    }

    public Location add(final Location loc) {
        double newX = this.x + loc.x;
        double newY = this.y + loc.y;
        double newZ = this.z + loc.z;
        return new Location(this.world, newX, newY, newZ, yaw, pitch);
    }

    public Vector3D getDirection() {
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

    public Vector3D toVector() {
        return new Vector3D(this.x, this.y, this.z);
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.world != null ? this.world.hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        hash = 19 * hash + Float.floatToIntBits(this.pitch);
        hash = 19 * hash + Float.floatToIntBits(this.yaw);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Location other = (Location) obj;

        if (!Objects.equals(this.world, other.world)) {
            return false;
        }
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        if (Float.floatToIntBits(this.pitch) != Float.floatToIntBits(other.pitch)) {
            return false;
        }
        return Float.floatToIntBits(this.yaw) == Float.floatToIntBits(other.yaw);
    }

    @Override
    public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}