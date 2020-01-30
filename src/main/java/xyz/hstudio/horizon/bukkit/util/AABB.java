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

public class AABB {

    public static final AABB collisionBox = new AABB(new Vector(-0.3, 0, -0.3), new Vector(0.3, 1.8, 0.3));
    public static final AABB waterCollisionBox = new AABB(new Vector(-0.299, 0.401, -0.299), new Vector(0.299, 1.399, 0.299));
    public static final AABB swimmingBox = new AABB(new Vector(-0.3, 0, -0.3), new Vector(0.3, 0.6, 0.3));

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

    public AABB(final Vector min, final Vector max) {
        this.minX = min.getX();
        this.minY = min.getY();
        this.minZ = min.getZ();
        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();
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

    public AABB add(final Vector vec) {
        return new AABB(this.minX + vec.getX(), this.minY + vec.getY(), this.minZ + vec.getZ(), this.maxX + vec.getX(), this.maxY + vec.getY(), this.maxZ + vec.getZ());
    }

    public AABB translateTo(final Vector vector) {
        return new AABB(vector.getX(), vector.getY(), vector.getZ(), vector.getX() + (maxX - minX), vector.getY() + (maxY - minY), vector.getZ() + (maxZ - minZ));
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
                    Vector position = new Vector(x, y, z);
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
    public Vector intersectsRay(Ray ray, float minDist, float maxDist) {
        Vector min = new Vector(this.minX, this.minY, this.minZ);
        Vector max = new Vector(this.maxX, this.maxY, this.maxZ);

        Vector invDir = new Vector(1F / ray.direction.getX(), 1F / ray.direction.getY(), 1F / ray.direction.getZ());

        boolean signDirX = invDir.getX() < 0;
        boolean signDirY = invDir.getY() < 0;
        boolean signDirZ = invDir.getZ() < 0;

        Vector bbox = signDirX ? max : min;
        double tmin = (bbox.getX() - ray.origin.getX()) * invDir.getX();
        bbox = signDirX ? min : max;
        double tmax = (bbox.getX() - ray.origin.getX()) * invDir.getX();
        bbox = signDirY ? max : min;
        double tymin = (bbox.getY() - ray.origin.getY()) * invDir.getY();
        bbox = signDirY ? min : max;
        double tymax = (bbox.getY() - ray.origin.getY()) * invDir.getY();

        if ((tmin > tymax) || (tymin > tmax)) {
            return null;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }

        bbox = signDirZ ? max : min;
        double tzmin = (bbox.getZ() - ray.origin.getZ()) * invDir.getZ();
        bbox = signDirZ ? min : max;
        double tzmax = (bbox.getZ() - ray.origin.getZ()) * invDir.getZ();

        if ((tmin > tzmax) || (tzmin > tmax)) {
            return null;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }
        if ((tmin < maxDist) && (tmax > minDist)) {
            return ray.getPointAtDistance(tmin);
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
        Set<Material> blocks = new HashSet<>();
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

    // TODO: Any ways to optimize it?
    public boolean isEmpty(final World world) {
        for (int x = NumberConversions.floor(this.minX); x < NumberConversions.ceil(this.maxX); x++) {
            for (int y = NumberConversions.floor(this.minY); y < NumberConversions.ceil(this.maxY); y++) {
                for (int z = NumberConversions.floor(this.minZ); z < NumberConversions.ceil(this.maxZ); z++) {
                    Block block = new Location(world, x, y, z).getBlock();
                    if (block == null || block.isEmpty() || !BlockUtils.isSolid(block.getType())) {
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
        return "minX:" + this.minX + ", minY:" + this.minY + ", minZ:" + this.minZ + ", maxX:" + this.maxX + ", maxY:" + this.maxY + ", maxZ:" + this.maxZ;
    }
}