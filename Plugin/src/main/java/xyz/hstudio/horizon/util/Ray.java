package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Ray implements Cloneable {

    @Getter
    private Vector3D origin, direction;

    public Vector3D getPointAtDistance(double distance) {
        Vector3D dir = new Vector3D(direction.getX(), direction.getY(), direction.getZ());
        Vector3D orig = new Vector3D(origin.getX(), origin.getY(), origin.getZ());
        return orig.add(dir.multiply(distance));
    }

    public Ray clone() {
        try {
            Ray clone = (Ray) super.clone();
            clone.origin = origin.clone();
            clone.direction = direction.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}