package xyz.hstudio.horizon.util;

import xyz.hstudio.horizon.wrapper.AccessorBase;

public class MathUtils {

    public static Vector3D getDirection(float yaw, float pitch) {
        float rotX = (float) Math.toRadians(yaw);
        float rotY = (float) Math.toRadians(pitch);
        double y = -AccessorBase.getInst().sin(rotY);
        double xz = AccessorBase.getInst().cos(rotY);
        double x = -xz * AccessorBase.getInst().sin(rotX);
        double z = xz * AccessorBase.getInst().cos(rotX);
        return new Vector3D(x, y, z);
    }
}