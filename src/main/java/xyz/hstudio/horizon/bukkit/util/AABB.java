package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class AABB {

    public static final AABB collisionBox = new AABB(new Vec3D(-0.3, 0, -0.3), new Vec3D(0.3, 1.8, 0.3));
    public static final AABB waterCollisionBox = new AABB(new Vec3D(-0.299, 0.401, -0.299), new Vec3D(0.299, 1.399, 0.299));
    public static final AABB swimmingBox = new AABB(new Vec3D(-0.3, 0, -0.3), new Vec3D(0.3, 0.6, 0.3));

    public final double minX, minY, minZ;
    public final double maxX, maxY, maxZ;

    public AABB(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AABB(final Vec3D min, final Vec3D max) {
        this.minX = min.x;
        this.minY = min.y;
        this.minZ = min.z;
        this.maxX = max.x;
        this.maxY = max.y;
        this.maxZ = max.z;
    }

    public AABB expand(final double x, final double y, final double z) {
        double minX = this.minX - x;
        double minY = this.minY - y;
        double minZ = this.minZ - z;
        double maxX = this.maxX + x;
        double maxY = this.maxY + y;
        double maxZ = this.maxZ + z;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AABB shrink(final double x, final double y, final double z) {
        double minX = this.minX + x;
        double minY = this.minY + y;
        double minZ = this.minZ + z;
        double maxX = this.maxX - x;
        double maxY = this.maxY - y;
        double maxZ = this.maxZ - z;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public AABB add(final double x, final double y, final double z) {
        return new AABB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    public AABB add(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        return new AABB(this.minX + minX, this.minY + minY, this.minZ + minZ, this.maxX + maxX, this.maxY + maxY, this.maxZ + maxZ);
    }

    public AABB add(final Vec3D vec) {
        return new AABB(this.minX + vec.x, this.minY + vec.y, this.minZ + vec.z, this.maxX + vec.x, this.maxY + vec.y, this.maxZ + vec.z);
    }

    public AABB translateTo(final Vec3D vector) {
        return new AABB(vector.x, vector.y, vector.z, vector.x + (maxX - minX), vector.y + (maxY - minY), vector.z + (maxZ - minZ));
    }

    public boolean isColliding(final AABB other) {
        if (this.maxX < other.minX || minX > other.maxX) {
            return false;
        }
        if (maxY < other.minY || minY > other.maxY) {
            return false;
        }
        return !(maxZ < other.minZ) && !(minZ > other.maxZ);
    }

    public AABB highlight(final World world, final double accuracy) {
        for (double x = this.minX; x <= this.maxX; x += accuracy) {
            for (double y = this.minY; y <= this.maxY; y += accuracy) {
                for (double z = this.minZ; z <= this.maxZ; z += accuracy) {
                    Vec3D position = new Vec3D(x, y, z);
                    world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 1);
                    world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 1);
                }
            }
        }
        return this;
    }

    /**
     * Calculates intersection with the given ray between a certain distance
     * interval.
     *
     * @author Islandscout, MrCraftGoo
     */
    public Vec3D intersectsRay(final Ray ray, final float minDist, final float maxDist) {
        Vec3D min = new Vec3D(this.minX, this.minY, this.minZ);
        Vec3D max = new Vec3D(this.maxX, this.maxY, this.maxZ);
        Vec3D invDir = new Vec3D(1F / ray.direction.x, 1F / ray.direction.y, 1F / ray.direction.z);
        boolean signDirX = invDir.x < 0;
        boolean signDirY = invDir.y < 0;
        boolean signDirZ = invDir.z < 0;
        Vec3D bbox = signDirX ? max : min;
        double txmin = (bbox.x - ray.origin.x) * invDir.x;
        bbox = signDirX ? min : max;
        double txmax = (bbox.x - ray.origin.x) * invDir.x;
        bbox = signDirY ? max : min;
        double tymin = (bbox.y - ray.origin.y) * invDir.y;
        bbox = signDirY ? min : max;
        double tymax = (bbox.y - ray.origin.y) * invDir.y;
        if (txmin > tymax || tymin > txmax) {
            return null;
        }
        if (tymin > txmin) {
            txmin = tymin;
        }
        if (tymax < txmax) {
            txmax = tymax;
        }
        bbox = signDirZ ? max : min;
        double tzmin = (bbox.z - ray.origin.z) * invDir.z;
        bbox = signDirZ ? min : max;
        double tzmax = (bbox.z - ray.origin.z) * invDir.z;
        if (txmin > tzmax || tzmin > txmax) {
            return null;
        }
        if (tzmin > txmin) {
            txmin = tzmin;
        }
        if (tzmax < txmax) {
            txmax = tzmax;
        }
        if (txmin < maxDist && txmax > minDist) {
            return ray.getPointAtDistance(txmin);
        }
        return null;
    }

    // TODO: Any ways to optimize it?
    public List<Block> getBlocks(final World world) {
        List<Block> blocks = new ArrayList<>();
        for (int x = NumberConversions.floor(this.minX); x < NumberConversions.ceil(this.maxX); x++) {
            for (int y = NumberConversions.floor(this.minY); y < NumberConversions.ceil(this.maxY); y++) {
                for (int z = NumberConversions.floor(this.minZ); z < NumberConversions.ceil(this.maxZ); z++) {
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
        Set<Material> blocks = EnumSet.noneOf(Material.class);
        for (int x = NumberConversions.floor(this.minX); x < NumberConversions.ceil(this.maxX); x++) {
            for (int y = NumberConversions.floor(this.minY); y < NumberConversions.ceil(this.maxY); y++) {
                for (int z = NumberConversions.floor(this.minZ); z < NumberConversions.ceil(this.maxZ); z++) {
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

    public double distance(final Vec3D vector) {
        double distX = Math.max(this.minX - vector.x, Math.max(0, vector.x - this.maxX));
        double distY = Math.max(this.minY - vector.y, Math.max(0, vector.y - this.maxY));
        double distZ = Math.max(this.minZ - vector.z, Math.max(0, vector.z - this.maxZ));
        return Math.sqrt(NumberConversions.square(distX) + NumberConversions.square(distY) + NumberConversions.square(distZ));
    }

    @Override
    public String toString() {
        return "minX:" + this.minX + ", minY:" + this.minY + ", minZ:" + this.minZ + ", maxX:" + this.maxX + ", maxY:" + this.maxY + ", maxZ:" + this.maxZ;
    }
}