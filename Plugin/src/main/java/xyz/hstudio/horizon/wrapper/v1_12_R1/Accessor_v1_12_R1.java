package xyz.hstudio.horizon.wrapper.v1_12_R1;

import net.minecraft.server.v1_12_R1.MathHelper;
import xyz.hstudio.horizon.wrapper.AccessorBase;

public class Accessor_v1_12_R1 extends AccessorBase {
    @Override
    public float sin(float v) {
        return MathHelper.sin(v);
    }

    @Override
    public float cos(float v) {
        return MathHelper.cos(v);
    }
}