package xyz.hstudio.horizon.util;

import lombok.Getter;

import java.util.Objects;

public class Ray2D {

    @Getter
    protected final Vector2D origin, direction;

    public Ray2D(Vector2D origin, Vector2D direction) {
        this.origin = origin;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ray2D)) {
            return false;
        }
        Ray2D other = (Ray2D) obj;
        return Objects.equals(origin, other.origin) && Objects.equals(direction, other.direction);
    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    public class Tracer {
        public double x, y, total;

        public Tracer() {
            x = origin.x;
            y = origin.y;
        }

        public double trace(double distance) {
            x += direction.x * distance;
            y += direction.y * distance;
            return total += distance;
        }
    }
}