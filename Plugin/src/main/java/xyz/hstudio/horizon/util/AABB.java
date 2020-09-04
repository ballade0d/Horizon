package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
public class AABB {

    @Getter
    private Vector3D min;
    @Getter
    private Vector3D max;

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
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AABB aabb = (AABB) obj;
        return Objects.equals(min, aabb.min) && Objects.equals(max, aabb.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
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