package xyz.hstudio.horizon.util.wrap;

import org.bukkit.Effect;
import org.bukkit.World;
import xyz.hstudio.horizon.util.collect.Pair;

public class Ray implements Cloneable {

    public Vector3D origin;
    public Vector3D direction;

    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3D getPointAtDistance(final double distance) {
        Vector3D direction = this.direction.clone();
        Vector3D origin = this.origin.clone();
        return origin.add(direction.multiply(distance));
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

    public Pair<Vector3D, Vector3D> closestPointsBetweenLines(final Ray other) {
        Vector3D n1 = direction.clone().crossProduct(other.direction.clone().crossProduct(direction));
        Vector3D n2 = other.direction.clone().crossProduct(direction.clone().crossProduct(other.direction));
        Vector3D c1 = origin.clone().add(direction.clone().multiply(other.origin.clone().subtract(origin).dot(n2) / direction.dot(n2)));
        Vector3D c2 = other.origin.clone().add(other.direction.clone().multiply(origin.clone().subtract(other.origin).dot(n1) / other.direction.dot(n1)));
        return new Pair<>(c1, c2);
    }

    public String toString() {
        return "origin: " + origin + " direction: " + direction;
    }
}