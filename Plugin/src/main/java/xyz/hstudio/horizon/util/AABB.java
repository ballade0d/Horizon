package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
public class AABB {

    @Getter
    protected final Vector3D min, max;

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));
    }

    public AABB add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        min.add(minX, minY, minZ);
        max.add(maxX, maxY, maxZ);
        return this;
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