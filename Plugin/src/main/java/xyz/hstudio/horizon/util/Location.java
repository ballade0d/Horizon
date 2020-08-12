package xyz.hstudio.horizon.util;

import lombok.Getter;
import lombok.Setter;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.Objects;

public class Location extends Vec3D implements Cloneable {

    @Getter
    private final WorldBase world;
    @Getter
    @Setter
    private float yaw, pitch;

    public Location(WorldBase world, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.world = world;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location add(Location loc) {
        x += loc.x;
        y += loc.y;
        z += loc.z;
        yaw += loc.yaw;
        pitch += loc.pitch;
        return this;
    }

    public Location subtract(Location loc) {
        x -= loc.x;
        y -= loc.y;
        z -= loc.z;
        yaw -= loc.yaw;
        pitch -= loc.pitch;
        return this;
    }

    public Vec3D getDirection() {
        Vec3D vector = new Vec3D();
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        vector.setY(-AccessorBase.getInst().sin(rotY));
        double xz = AccessorBase.getInst().cos(rotY);
        vector.setX(-xz * AccessorBase.getInst().sin(rotX));
        vector.setZ(xz * AccessorBase.getInst().cos(rotX));
        return vector;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Location other = (Location) obj;
            if (!Objects.equals(world, other.world)) {
                return false;
            } else if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
                return false;
            } else if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
                return false;
            } else if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
                return false;
            } else if (Float.floatToIntBits(pitch) != Float.floatToIntBits(other.pitch)) {
                return false;
            } else {
                return Float.floatToIntBits(yaw) == Float.floatToIntBits(other.yaw);
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (world != null ? world.hashCode() : 0);
        hash = 19 * hash + (int) (Double.doubleToLongBits(x) ^ Double.doubleToLongBits(x) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(y) ^ Double.doubleToLongBits(y) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(z) ^ Double.doubleToLongBits(z) >>> 32);
        hash = 19 * hash + Float.floatToIntBits(pitch);
        hash = 19 * hash + Float.floatToIntBits(yaw);
        return hash;
    }

    @Override
    public Location clone() {
        return (Location) super.clone();
    }
}