package xyz.hstudio.horizon.util.wrap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class AABB {

    public static final AABB NORMAL_BOX = new AABB(new Vector3D(-0.3, 0, -0.3), new Vector3D(0.3, 1.8, 0.3));
    public static final AABB WATER_BOX = new AABB(new Vector3D(-0.299, 0.401, -0.299), new Vector3D(0.299, 1.399, 0.299));
    public static final AABB SWIM_BOX = new AABB(new Vector3D(-0.3, 0, -0.3), new Vector3D(0.3, 0.6, 0.3));

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

    public AABB(final Vector3D min, final Vector3D max) {
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

    public AABB add(final Vector3D vec) {
        return new AABB(this.minX + vec.x, this.minY + vec.y, this.minZ + vec.z, this.maxX + vec.x, this.maxY + vec.y, this.maxZ + vec.z);
    }

    public AABB translateTo(final Vector3D vector) {
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

    /**
     * Calculates intersection with the given ray between a certain distance
     * interval.
     *
     * @author Islandscout, MrCraftGoo
     */
    public Vector3D intersectsRay(final Ray ray, final float minDist, final float maxDist) {
        Vector3D min = new Vector3D(this.minX, this.minY, this.minZ);
        Vector3D max = new Vector3D(this.maxX, this.maxY, this.maxZ);
        Vector3D invDir = new Vector3D(1F / ray.direction.x, 1F / ray.direction.y, 1F / ray.direction.z);
        boolean signDirX = invDir.x < 0;
        boolean signDirY = invDir.y < 0;
        boolean signDirZ = invDir.z < 0;
        Vector3D bbox = signDirX ? max : min;
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

    public boolean betweenRays(final Vector3D pos, final Vector3D dir1, final Vector3D dir2) {
        if (dir1.dot(dir2) > 0.999) {
            return this.intersectsRay(new Ray(pos, dir2), 0, Float.MAX_VALUE) != null;
        } else {
            Vector3D planeNormal = dir2.clone().crossProduct(dir1);
            Vector3D[] vertices = this.getVertices();
            boolean hitPlane = false;
            boolean above = false;
            boolean below = false;
            for (Vector3D vertex : vertices) {
                vertex.subtract(pos);
                if (!hitPlane) {
                    if (vertex.dot(planeNormal) > 0) {
                        above = true;
                    } else {
                        below = true;
                    }
                    if (above && below) {
                        hitPlane = true;
                    }
                }
            }
            if (!hitPlane) {
                return false;
            }
            Vector3D extraDirToDirNormal = planeNormal.clone().crossProduct(dir2);
            Vector3D dirToExtraDirNormal = dir1.clone().crossProduct(planeNormal);
            boolean betweenVectors = false;
            boolean frontOfExtraDirToDir = false;
            boolean frontOfDirToExtraDir = false;
            for (Vector3D vertex : vertices) {
                if (!frontOfExtraDirToDir && vertex.dot(extraDirToDirNormal) >= 0) {
                    frontOfExtraDirToDir = true;
                }
                if (!frontOfDirToExtraDir && vertex.dot(dirToExtraDirNormal) >= 0) {
                    frontOfDirToExtraDir = true;
                }

                if (frontOfExtraDirToDir && frontOfDirToExtraDir) {
                    betweenVectors = true;
                    break;
                }
            }
            return betweenVectors;
        }
    }

    public Vector3D[] getVertices() {
        return new Vector3D[]{new Vector3D(minX, minY, minZ),
                new Vector3D(minX, minY, maxZ),
                new Vector3D(minX, maxY, minZ),
                new Vector3D(minX, maxY, maxZ),
                new Vector3D(maxX, minY, minZ),
                new Vector3D(maxX, minY, maxZ),
                new Vector3D(maxX, maxY, minZ),
                new Vector3D(maxX, maxY, maxZ)};
    }

    public List<IWrappedBlock> getBlocks(final World world) {
        List<IWrappedBlock> blocks = new ArrayList<>();
        for (int x = NumberConversions.floor(this.minX); x < NumberConversions.ceil(this.maxX); x++) {
            for (int y = NumberConversions.floor(this.minY); y < NumberConversions.ceil(this.maxY); y++) {
                for (int z = NumberConversions.floor(this.minZ); z < NumberConversions.ceil(this.maxZ); z++) {
                    IWrappedBlock block = new Location(world, x, y, z).getBlock();
                    if (block == null || block.getType() == Material.AIR) {
                        continue;
                    }
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public Set<Material> getMaterials(final World world) {
        Set<Material> blocks = EnumSet.noneOf(Material.class);
        for (int x = NumberConversions.floor(this.minX); x < NumberConversions.ceil(this.maxX); x++) {
            for (int y = NumberConversions.floor(this.minY); y < NumberConversions.ceil(this.maxY); y++) {
                for (int z = NumberConversions.floor(this.minZ); z < NumberConversions.ceil(this.maxZ); z++) {
                    IWrappedBlock block = new Location(world, x, y, z).getBlock();
                    if (block == null || block.getType() == Material.AIR) {
                        continue;
                    }
                    blocks.add(block.getType());
                }
            }
        }
        return blocks;
    }

    public double distance(final Vector3D vector) {
        double distX = Math.max(this.minX - vector.x, Math.max(0, vector.x - this.maxX));
        double distY = Math.max(this.minY - vector.y, Math.max(0, vector.y - this.maxY));
        double distZ = Math.max(this.minZ - vector.z, Math.max(0, vector.z - this.maxZ));
        return Math.sqrt(NumberConversions.square(distX) + NumberConversions.square(distY) + NumberConversions.square(distZ));
    }

    public Vector3D getMin() {
        return new Vector3D(minX, minY, minZ);
    }

    public Vector3D getMax() {
        return new Vector3D(maxX, maxY, maxZ);
    }

    public void highlight(World world, double accuracy) {
        for (double x = minX; x <= maxX; x += accuracy) {
            for (double y = minY; y <= maxY; y += accuracy) {
                for (double z = minZ; z <= maxZ; z += accuracy) {
                    Vector position = new Vector(x, y, z);
                    world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 1);
                    world.playEffect(position.toLocation(world), Effect.COLOURED_DUST, 1);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        long var1 = Double.doubleToLongBits(this.minX);
        int var3 = (int) (var1 ^ var1 >>> 32);
        var1 = Double.doubleToLongBits(this.minY);
        var3 = 31 * var3 + (int) (var1 ^ var1 >>> 32);
        var1 = Double.doubleToLongBits(this.minZ);
        var3 = 31 * var3 + (int) (var1 ^ var1 >>> 32);
        var1 = Double.doubleToLongBits(this.maxX);
        var3 = 31 * var3 + (int) (var1 ^ var1 >>> 32);
        var1 = Double.doubleToLongBits(this.maxY);
        var3 = 31 * var3 + (int) (var1 ^ var1 >>> 32);
        var1 = Double.doubleToLongBits(this.maxZ);
        var3 = 31 * var3 + (int) (var1 ^ var1 >>> 32);
        return var3;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof AABB)) {
            return false;
        } else {
            AABB aabb = (AABB) other;
            return Double.compare(aabb.minX, this.minX) == 0 && Double.compare(aabb.minY, this.minY) == 0 &&
                    Double.compare(aabb.minZ, this.minZ) == 0 && Double.compare(aabb.maxX, this.maxX) == 0 &&
                    Double.compare(aabb.maxY, this.maxY) == 0 && Double.compare(aabb.maxZ, this.maxZ) == 0;
        }
    }

    @Override
    public String toString() {
        return "minX:" + this.minX + ", minY:" + this.minY + ", minZ:" + this.minZ + ", maxX:" + this.maxX + ", maxY:" + this.maxY + ", maxZ:" + this.maxZ;
    }
}