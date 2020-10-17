package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
public class Ray3D {

    @Getter
    protected final Vector3D origin, direction;

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
}