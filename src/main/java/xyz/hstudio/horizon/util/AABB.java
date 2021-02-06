package xyz.hstudio.horizon.util;

import org.bukkit.Material;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockWrapper;
import xyz.hstudio.horizon.wrapper.WorldWrapper;

import java.util.*;

public class AABB {

    public final Vector3D min, max;

    public AABB(Vector3D min, Vector3D max) {
        this.min = min;
        this.max = max;
    }

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));
    }

    public static AABB def() {
        return new AABB(new Vector3D(-0.3, 0, -0.3), new Vector3D(0.3, 1.8, 0.3));
    }

    public AABB add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        min.add(minX, minY, minZ);
        max.add(maxX, maxY, maxZ);
        return this;
    }

    public AABB add(Vector3D vec) {
        min.add(vec);
        max.add(vec);
        return this;
    }

    public void shrink(double x, double y, double z) {
        Vector3D subtraction = new Vector3D(x, y, z);
        min.add(subtraction);
        max.subtract(subtraction);
    }

    public AABB plus(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AABB(min.x + minX, min.y + minY, min.z + minZ, max.x + maxX, max.y + maxY, max.z + maxZ);
    }

    public AABB plus(Vector3D vec) {
        return plus(vec.x, vec.y, vec.z, vec.x, vec.y, vec.z);
    }

    public AABB expand(double x, double y, double z) {
        min.subtract(x, y, z);
        max.add(x, y, z);
        return this;
    }

    public boolean collides(AABB other) {
        if (max.x < other.min.x || min.x > other.max.x) {
            return false;
        }
        if (max.y < other.min.y || min.y > other.max.y) {
            return false;
        }
        return !(max.z < other.min.z) && !(min.z > other.max.z);
    }

    public List<BlockWrapper> blocks(WorldWrapper world) {
        List<BlockWrapper> blocks = new ArrayList<>();
        for (int x = NumberConversions.floor(min.x); x < NumberConversions.ceil(max.x); x++) {
            for (int y = NumberConversions.floor(min.y); y < NumberConversions.ceil(max.y); y++) {
                for (int z = NumberConversions.floor(min.z); z < NumberConversions.ceil(max.z); z++) {
                    BlockWrapper block = world.getBlock(x, y, z);
                    if (block == null) {
                        continue;
                    }
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public Set<Material> materials(WorldWrapper world) {
        Set<Material> mats = EnumSet.noneOf(Material.class);
        for (int x = NumberConversions.floor(min.x); x < NumberConversions.ceil(max.x); x++) {
            for (int y = NumberConversions.floor(min.y); y < NumberConversions.ceil(max.y); y++) {
                for (int z = NumberConversions.floor(min.z); z < NumberConversions.ceil(max.z); z++) {
                    BlockWrapper block = world.getBlock(x, y, z);
                    if (block == null) {
                        continue;
                    }
                    mats.add(block.type());
                }
            }
        }
        return mats;
    }

    /**
     * Calculates intersection with the given ray between a certain distance interval.
     *
     * @param ray     incident ray
     * @param minDist minimum distance
     * @param maxDist maximum distance
     * @return intersection point on the bounding box (only the first is
     * returned) or null if no intersection
     */
    public Vector3D intersectsRay(Ray3D ray, float minDist, float maxDist) {
        Vector3D invDir = new Vector3D(1f / ray.direction.x, 1f / ray.direction.y, 1f / ray.direction.z);

        boolean signDirX = invDir.x < 0;
        boolean signDirY = invDir.y < 0;
        boolean signDirZ = invDir.z < 0;

        Vector3D bbox = signDirX ? max : min;
        double tMin = (bbox.x - ray.origin.x) * invDir.x;
        bbox = signDirX ? min : max;
        double tMax = (bbox.x - ray.origin.x) * invDir.x;
        bbox = signDirY ? max : min;
        double tyMin = (bbox.y - ray.origin.y) * invDir.y;
        bbox = signDirY ? min : max;
        double tyMax = (bbox.y - ray.origin.y) * invDir.y;

        if (tMin > tyMax || tyMin > tMax) {
            return null;
        }
        if (tyMin > tMin) {
            tMin = tyMin;
        }
        if (tyMax < tMax) {
            tMax = tyMax;
        }

        bbox = signDirZ ? max : min;
        double tzMin = (bbox.z - ray.origin.z) * invDir.z;
        bbox = signDirZ ? min : max;
        double tzMax = (bbox.z - ray.origin.z) * invDir.z;

        if (tMin > tzMax || tzMin > tMax) {
            return null;
        }
        if (tzMin > tMin) {
            tMin = tzMin;
        }
        if (tzMax < tMax) {
            tMax = tzMax;
        }
        if (tMin < maxDist && tMax > minDist) {
            return ray.getPointAtDistance(tMin);
        }
        return null;
    }

    public List<AABB> getBlockAABBs(HPlayer p, WorldWrapper world, Material first, Material... exemptedMats) {
        Set<Material> exempt = EnumSet.of(first, exemptedMats);
        List<AABB> aabbs = new ArrayList<>();

        // gotta do this to catch fences and cobble walls
        AABB expanded = this.add(0, -1, 0, 0, 0, 0);
        List<BlockWrapper> blocks = expanded.blocks(world);

        for (BlockWrapper b : blocks) {
            if (exempt.contains(b.type())) {
                continue;
            }
            AABB[] bAABBs = b.boxes(p);
            for (AABB aabb : bAABBs) {
                if (this.collides(aabb)) {
                    aabbs.add(aabb);
                }
            }
        }
        return aabbs;
    }

    public List<AABB> getBlockAABBs(HPlayer p, WorldWrapper world) {
        List<AABB> aabbs = new ArrayList<>();

        // gotta do this to catch fences and cobble walls
        AABB expanded = this.add(0, -1, 0, 0, 0, 0);
        List<BlockWrapper> blocks = expanded.blocks(world);

        for (BlockWrapper b : blocks) {
            AABB[] bAABBs = b.boxes(p);
            for (AABB aabb : bAABBs) {
                if (this.collides(aabb)) {
                    aabbs.add(aabb);
                }
            }
        }
        return aabbs;
    }

    public Set<Direction> touchingFaces(HPlayer p, WorldWrapper world, double borderSize) {
        Vector3D min = this.min.plus(new Vector3D(-borderSize, -borderSize, -borderSize));
        Vector3D max = this.max.plus(new Vector3D(borderSize, borderSize, borderSize));
        AABB bigBox = new AABB(min, max);
        Set<Direction> directions = EnumSet.noneOf(Direction.class);
        // The coordinates should be floored, but this works too.
        for (int x = (int) (min.x < 0 ? min.x - 1 : min.x); x <= max.x; x++) {
            // Always subtract 1 so that fences/walls can be checked
            for (int y = (int) min.y - 1; y <= max.y; y++) {
                for (int z = (int) (min.z < 0 ? min.z - 1 : min.z); z <= max.z; z++) {
                    BlockWrapper block = world.getBlock(x, y, z);
                    if (block == null) {
                        continue;
                    }
                    for (AABB blockBox : block.boxes(p)) {
                        if (blockBox.min.x > this.max.x && blockBox.min.x < bigBox.max.x) {
                            directions.add(Direction.EAST);
                        }
                        if (blockBox.min.y > this.max.y && blockBox.min.y < bigBox.max.y) {
                            directions.add(Direction.UP);
                        }
                        if (blockBox.min.z > this.max.z && blockBox.min.z < bigBox.max.z) {
                            directions.add(Direction.SOUTH);
                        }
                        if (blockBox.max.x > bigBox.min.x && blockBox.max.x < this.min.x) {
                            directions.add(Direction.WEST);
                        }
                        if (blockBox.max.y > bigBox.min.y && blockBox.max.y < this.min.y) {
                            directions.add(Direction.DOWN);
                        }
                        if (blockBox.max.z > bigBox.min.z && blockBox.max.z < this.min.z) {
                            directions.add(Direction.NORTH);
                        }
                    }
                }
            }
        }
        return directions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AABB)) {
            return false;
        }
        AABB other = (AABB) obj;
        return Objects.equals(min, other.min) && Objects.equals(max, other.max);
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        return result;
    }
}