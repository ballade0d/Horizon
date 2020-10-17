package xyz.hstudio.horizon.util;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.wrapper.BlockBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Location extends Vector3D {

    public final WorldBase world;
    public float yaw, pitch;

    public Location(WorldBase world, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.world = world;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    // Plus

    public Location plus(double x, double y, double z) {
        return new Location(world, this.x + x, this.y + y, this.z + z, yaw, pitch);
    }

    public Location plus(Location vec) {
        return plus(vec.x, vec.y, vec.z);
    }

    // Add

    public Location add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Location add(Location vec) {
        return add(vec.x, vec.y, vec.z);
    }

    // Minus

    public Location minus(double x, double y, double z) {
        return new Location(world, this.x - x, this.y - y, this.z - z, yaw, pitch);
    }

    public Location minus(Location vec) {
        return minus(vec.x, vec.y, vec.z);
    }

    // Subtract

    public Location subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Location subtract(Location loc) {
        return subtract(loc.x, loc.y, loc.z);
    }

    // Multiply

    public Location multiply(double v) {
        this.x *= v;
        this.y *= v;
        this.z *= v;
        return this;
    }

    // Divide

    public Location divide(double v) {
        this.x /= v;
        this.y /= v;
        this.z /= v;
        return this;
    }

    public Vector3D getDirection() {
        return MathUtils.getDirection(yaw, pitch);
    }

    public org.bukkit.Location toBukkit() {
        return new org.bukkit.Location(world.bukkit(), x, y, z, yaw, pitch);
    }

    public boolean isOnGround(HPlayer p, boolean ignoreOnGround, double depth) {
        Set<BlockBase> blocks = new HashSet<>();
        blocks.addAll(BlockUtils.getBlocksInLocation(this));
        blocks.addAll(BlockUtils.getBlocksInLocation(this.plus(0, -1, 0)));
        Set<BlockBase> blocksAbove = !ignoreOnGround ? null : BlockUtils.getBlocksInLocation(this);

        AABB underFeet = toAABB(0.6, 0).add(0, -depth, 0, 0, 0, 0);
        AABB topFeet = toAABB(0.6, 0).add(0, 0.00001, 0, 0, 0, 0);

        for (BlockBase block : blocks) {
            if (block.isLiquid() || !BlockUtils.isSolid(block)) {
                continue;
            }
            for (AABB bBox : block.boxes(p)) {
                if (!bBox.collides(underFeet)) {
                    continue;
                }
                if (!ignoreOnGround) {
                    return true;
                }
                for (BlockBase above : blocksAbove) {
                    if (above.isLiquid() || !BlockUtils.isSolid(above)) {
                        continue;
                    }
                    for (AABB aboveBox : above.boxes(p)) {
                        if (aboveBox.collides(topFeet)) {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public BlockBase getBlock() {
        return world.getBlock(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location other = (Location) obj;
        if (!Objects.equals(world, other.world)) {
            return false;
        } else if (Double.doubleToRawLongBits(x) != Double.doubleToRawLongBits(other.x)) {
            return false;
        } else if (Double.doubleToRawLongBits(y) != Double.doubleToRawLongBits(other.y)) {
            return false;
        } else if (Double.doubleToRawLongBits(z) != Double.doubleToRawLongBits(other.z)) {
            return false;
        } else if (Float.floatToRawIntBits(pitch) != Float.floatToRawIntBits(other.pitch)) {
            return false;
        } else return Float.floatToRawIntBits(yaw) == Float.floatToRawIntBits(other.yaw);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        result = 31 * result + Float.hashCode(yaw);
        result = 31 * result + Float.hashCode(pitch);
        return result;
    }
}