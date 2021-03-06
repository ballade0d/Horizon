package xyz.hstudio.horizon.util;

import org.bukkit.util.NumberConversions;

public class Vector2D implements Cloneable {

    public double x, y;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Plus

    public Vector2D plus(double x, double y) {
        return new Vector2D(this.x + x, this.y + y);
    }

    public Vector2D plus(Vector2D vec) {
        return plus(vec.x, vec.y);
    }

    // Add

    public Vector2D add(double x, double y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2D add(Vector2D vec) {
        return add(vec.x, vec.y);
    }

    // Subtract

    public Vector2D subtract(double x, double y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vector2D subtract(Vector2D vec) {
        return subtract(vec.x, vec.y);
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

    @Override
    public Vector2D clone() {
        try {
            return (Vector2D) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }
}