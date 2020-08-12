package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Ray implements Cloneable {

    @Getter
    private Vec3D origin, direction;

    public Vec3D getPointAtDistance(double distance) {
        Vec3D dir = new Vec3D(direction.getX(), direction.getY(), direction.getZ());
        Vec3D orig = new Vec3D(origin.getX(), origin.getY(), origin.getZ());
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