package xyz.hstudio.horizon.wrap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.enums.Version;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.v1_12_R1.WrappedBlock_v1_12_R1;
import xyz.hstudio.horizon.wrap.v1_13_R2.WrappedBlock_v1_13_R2;
import xyz.hstudio.horizon.wrap.v1_8_R3.WrappedBlock_v1_8_R3;

public interface IWrappedBlock {

    static IWrappedBlock create(final Block block) {
        if (block == null) {
            return null;
        }
        switch (Version.VERSION) {
            case v1_8_R3:
                return new WrappedBlock_v1_8_R3(block);
            case v1_12_R1:
                return new WrappedBlock_v1_12_R1(block);
            case v1_13_R2:
                return new WrappedBlock_v1_13_R2(block);
            default:
                return null;
        }
    }

    Material getType();

    boolean isSolid();

    boolean isLiquid();

    Object getData();

    AABB[] getBoxes(final HoriPlayer player);

    float getFriction();

    Location getPos();

    Vector3D getFlowDirection();
}