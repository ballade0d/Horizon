package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.util.NumberConversions;

public class Vec2D {

    public double x;
    public double y;

    public Vec2D(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2D(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2D(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public double distance(final Vec2D o) {
        return Math.sqrt(this.distanceSquared(o));
    }

    public double distanceSquared(final Vec2D o) {
        return NumberConversions.square(x - o.x) + NumberConversions.square(y - o.y);
    }

    @Override
    public String toString() {
        return x + ", " + y;
    }
}