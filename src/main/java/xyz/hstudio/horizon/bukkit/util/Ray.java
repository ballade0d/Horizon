package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Effect;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Ray implements Cloneable {

    public Vector origin;
    public Vector direction;

    public Ray(Vector origin, Vector direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector getPointAtDistance(double distance) {
        Vector dir = new Vector(direction.getX(), direction.getY(), direction.getZ());
        Vector orig = new Vector(origin.getX(), origin.getY(), origin.getZ());
        return orig.add(dir.multiply(distance));
    }

    public Ray clone() {
        Ray clone;
        try {
            clone = (Ray) super.clone();
            clone.origin = this.origin.clone();
            clone.direction = this.direction.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void highlight(final World world, final double blocksAway, final double accuracy) {
        for (double x = 0; x < blocksAway; x += accuracy) {
            world.playEffect(getPointAtDistance(x).toLocation(world), Effect.COLOURED_DUST, 1);
        }
    }

    public Pair<Vector, Vector> closestPointsBetweenLines(final Ray other) {
        Vector n1 = direction.clone().crossProduct(other.direction.clone().crossProduct(direction));
        Vector n2 = other.direction.clone().crossProduct(direction.clone().crossProduct(other.direction));
        Vector c1 = origin.clone().add(direction.clone().multiply(other.origin.clone().subtract(origin).dot(n2) / direction.dot(n2)));
        Vector c2 = other.origin.clone().add(other.direction.clone().multiply(origin.clone().subtract(other.origin).dot(n1) / other.direction.dot(n1)));
        return new Pair<>(c1, c2);
    }

    public String toString() {
        return "origin: " + origin + " direction: " + direction;
    }
}