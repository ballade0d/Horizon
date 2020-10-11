package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.util.NumberConversions;

@AllArgsConstructor
public class Vector2D {

    @Getter
    protected final double x, y;

    public Vector2D minus(Vector2D vec) {
        return new Vector2D(x - vec.x, y - vec.y);
    }

    public double distance(Vector2D vec) {
        return Math.sqrt(distanceSquared(vec));
    }

    public double distanceSquared(Vector2D vec) {
        return NumberConversions.square(x - vec.x) + NumberConversions.square(y - vec.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vector2D)) {
            return false;
        }
        Vector2D other = (Vector2D) obj;
        if (Double.doubleToRawLongBits(x) != Double.doubleToRawLongBits(other.x)) {
            return false;
        } else return Double.doubleToRawLongBits(y) == Double.doubleToRawLongBits(other.y);
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        return result;
    }
}