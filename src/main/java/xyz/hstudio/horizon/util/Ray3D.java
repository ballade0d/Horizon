package xyz.hstudio.horizon.util;

import lombok.Getter;

import java.util.Objects;

public class Ray3D {

    @Getter
    protected final Vector3D origin, direction;

    public Ray3D(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3D getPointAtDistance(double distance) {
        return new Vector3D(
                origin.x + direction.x * distance,
                origin.y + direction.y * distance,
                origin.z + direction.z * distance
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ray3D)) {
            return false;
        }
        Ray3D other = (Ray3D) obj;
        return Objects.equals(origin, other.origin) && Objects.equals(direction, other.direction);
    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    public class Tracer {
        public double x, y, z;

        public Tracer() {
            x = origin.x;
            y = origin.y;
            z = origin.z;
        }

        public void trace(double distance) {
            x += direction.x * distance;
            y += direction.y * distance;
            z += direction.z * distance;
        }
    }
}