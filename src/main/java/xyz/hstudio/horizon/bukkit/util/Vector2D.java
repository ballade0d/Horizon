package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.util.NumberConversions;

public class Vector2D {

    public double x;
    public double y;

    public Vector2D(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public double distance(final Vector2D o) {
        return Math.sqrt(this.distanceSquared(o));
    }

    public double distanceSquared(final Vector2D o) {
        return NumberConversions.square(x - o.x) + NumberConversions.square(y - o.y);
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}