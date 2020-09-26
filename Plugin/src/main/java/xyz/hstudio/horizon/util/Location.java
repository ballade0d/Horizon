package xyz.hstudio.horizon.util;

import lombok.Getter;
import lombok.Setter;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.Objects;

public class Location extends Vector3D implements Cloneable {

    @Getter
    protected final WorldBase world;
    @Getter
    @Setter
    protected float yaw, pitch;

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

    public Vector3D getDirection() {
        Vector3D vector = new Vector3D();
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        vector.y = -AccessorBase.getInst().sin(rotY);
        double xz = AccessorBase.getInst().cos(rotY);
        vector.x = -xz * AccessorBase.getInst().sin(rotX);
        vector.z = xz * AccessorBase.getInst().cos(rotX);
        return vector;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location other = (Location) obj;
        if (!Objects.equals(world, other.world)) {
            return false;
        } else if (Double.doubleToRawLongBits(x) != Double.doubleToRawLongBits(other.x)) {
            return false;
        } else if (Double.doubleToRawLongBits(y) != Double.doubleToRawLongBits(other.y)) {
            return false;
        } else if (Double.doubleToRawLongBits(z) != Double.doubleToRawLongBits(other.z)) {
            return false;
        } else if (Float.floatToRawIntBits(pitch) != Float.floatToRawIntBits(other.pitch)) {
            return false;
        } else return Float.floatToRawIntBits(yaw) == Float.floatToRawIntBits(other.yaw);
    }

    @Override
    public int hashCode() {
        int result = world.hashCode();
        result = 31 * result + Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        result = 31 * result + Float.hashCode(yaw);
        result = 31 * result + Float.hashCode(pitch);
        return result;
    }

    @Override
    public Location clone() {
        return (Location) super.clone();
    }
}