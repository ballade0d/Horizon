package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
public class AABB {

    @Getter
    @Setter
    protected Vector3D min, max;

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this(new Vector3D(minX, minY, minZ), new Vector3D(maxX, maxY, maxZ));
    }

    public AABB translate(Vector3D vec) {
        min.add(vec);
        max.add(vec);
        return this;
    }

    public boolean isColliding(AABB other) {
        if (max.x < other.min.x || min.x > other.max.x) {
            return false;
        }
        if (max.y < other.min.y || min.y > other.max.y) {
            return false;
        }
        return !(max.z < other.min.z) && !(min.z > other.max.z);
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

    @Override
    public AABB clone() {
        try {
            AABB clone = (AABB) super.clone();
            clone.min = min.clone();
            clone.max = max.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}