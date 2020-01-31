package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Effect;
import org.bukkit.World;

public class Ray implements Cloneable {

    public Vec3D origin;
    public Vec3D direction;

    public Ray(Vec3D origin, Vec3D direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vec3D getPointAtDistance(final double distance) {
        Vec3D dir = new Vec3D(direction.x, direction.y, direction.z);
        Vec3D orig = new Vec3D(origin.x, origin.y, origin.z);
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

    public Pair<Vec3D, Vec3D> closestPointsBetweenLines(final Ray other) {
        Vec3D n1 = direction.clone().crossProduct(other.direction.clone().crossProduct(direction));
        Vec3D n2 = other.direction.clone().crossProduct(direction.clone().crossProduct(other.direction));
        Vec3D c1 = origin.clone().add(direction.clone().multiply(other.origin.clone().subtract(origin).dot(n2) / direction.dot(n2)));
        Vec3D c2 = other.origin.clone().add(other.direction.clone().multiply(origin.clone().subtract(other.origin).dot(n1) / other.direction.dot(n1)));
        return new Pair<>(c1, c2);
    }

    public String toString() {
        return "origin: " + origin + " direction: " + direction;
    }
}