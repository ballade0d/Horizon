package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AxisAlignedBB {

    public final double minX, minY, minZ;
    public final double maxX, maxY, maxZ;

    public AxisAlignedBB(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AxisAlignedBB(final Vector min, final Vector max) {
        this.minX = min.getX();
        this.minY = min.getY();
        this.minZ = min.getZ();
        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();
    }

    public AxisAlignedBB expand(final double x, final double y, final double z) {
        double minX = this.minX - x;
        double minY = this.minY - y;
        double minZ = this.minZ - z;
        double maxX = this.maxX + x;
        double maxY = this.maxY + y;
        double maxZ = this.maxZ + z;
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB shrink(final double x, final double y, final double z) {
        double minX = this.minX + x;
        double minY = this.minY + y;
        double minZ = this.minZ + z;
        double maxX = this.maxX - x;
        double maxY = this.maxY - y;
        double maxZ = this.maxZ - z;
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AxisAlignedBB add(final double x, final double y, final double z) {
        return new AxisAlignedBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public AxisAlignedBB add(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        return new AxisAlignedBB(this.minX + minX, this.minY + minY, this.minZ + minZ, this.maxX + maxX, this.maxY + maxY, this.maxZ + maxZ);
    }

    public AxisAlignedBB add(final Vector vec) {
        return new AxisAlignedBB(this.minX + vec.getX(), this.minY + vec.getY(), this.minZ + vec.getZ(), this.maxX + vec.getX(), this.maxY + vec.getY(), this.maxZ + vec.getZ());
    }

    public AxisAlignedBB highlight(final World world, final double accuracy) {
        for (double x = this.minX; x <= this.maxX; x += accuracy) {
            for (double y = this.minY; y <= this.maxY; y += accuracy) {
                for (double z = this.minZ; z <= this.maxZ; z += accuracy) {
                    Vector position = new Vector(x, y, z);
                    world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 1);
                    world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 1);
                }
            }
        }
        return this;
    }

    // TODO: Any ways to optimize it?
    public List<Block> getBlocks(final World world) {
        List<Block> blocks = new ArrayList<>();
        for (int x = (int) Math.floor(this.minX); x < (int) Math.ceil(this.maxX); x++) {
            for (int y = (int) Math.floor(this.minY); y < (int) Math.ceil(this.maxY); y++) {
                for (int z = (int) Math.floor(this.minZ); z < (int) Math.ceil(this.maxZ); z++) {
                    Block block = new Location(world, x, y, z).getBlock();
                    if (block == null || block.isEmpty()) {
                        continue;
                    }
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    // TODO: Any ways to optimize it?
    public Set<Material> getMaterials(final World world) {
        Set<Material> blocks = new HashSet<>();
        for (int x = (int) Math.floor(this.minX); x < (int) Math.ceil(this.maxX); x++) {
            for (int y = (int) Math.floor(this.minY); y < (int) Math.ceil(this.maxY); y++) {
                for (int z = (int) Math.floor(this.minZ); z < (int) Math.ceil(this.maxZ); z++) {
                    Block block = new Location(world, x, y, z).getBlock();
                    if (block == null || block.isEmpty()) {
                        continue;
                    }
                    blocks.add(block.getType());
                }
            }
        }
        return blocks;
    }

    // TODO: Any ways to optimize it?
    public boolean isEmpty(final World world) {
        for (int x = (int) Math.floor(this.minX); x < (int) Math.ceil(this.maxX); x++) {
            for (int y = (int) Math.floor(this.minY); y < (int) Math.ceil(this.maxY); y++) {
                for (int z = (int) Math.floor(this.minZ); z < (int) Math.ceil(this.maxZ); z++) {
                    Block block = new Location(world, x, y, z).getBlock();
                    if (block == null || block.isEmpty() || !block.getType().isSolid()) {
                        continue;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public double distance(final Vector vector) {
        double distX = Math.max(this.minX - vector.getX(), Math.max(0, vector.getX() - this.maxX));
        double distY = Math.max(this.minY - vector.getY(), Math.max(0, vector.getY() - this.maxY));
        double distZ = Math.max(this.minZ - vector.getZ(), Math.max(0, vector.getZ() - this.maxZ));
        return Math.sqrt(NumberConversions.square(distX) + NumberConversions.square(distY) + NumberConversions.square(distZ));
    }

    @Override
    public String toString() {
        return "minX:" + this.minX + ", minY:" + this.minY + ", minZ:" + this.minZ + ", maxX:" + this.maxX + ", maxY:" + this.maxY + ", maxZ" + this.maxZ;
    }
}