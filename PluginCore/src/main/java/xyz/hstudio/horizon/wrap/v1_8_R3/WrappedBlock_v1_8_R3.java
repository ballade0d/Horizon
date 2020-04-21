package xyz.hstudio.horizon.wrap.v1_8_R3;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.ArrayList;
import java.util.List;

public class WrappedBlock_v1_8_R3 implements IWrappedBlock {

    private final WorldServer world;
    private final BlockPosition bPos;
    private final IBlockData data;
    private final Material type;
    private final int x, y, z;

    public WrappedBlock_v1_8_R3(final org.bukkit.block.Block block) {
        this.world = ((CraftWorld) block.getWorld()).getHandle();
        this.bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        this.data = world.getType(bPos);
        this.type = CraftMagicNumbers.getMaterial(data.getBlock());
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    @Override
    public Material getType() {
        return this.type;
    }

    @Override
    public boolean isSolid() {
        return this.data.getBlock().getMaterial().isSolid();
    }

    @Override
    public boolean isLiquid() {
        return this.data.getBlock().getMaterial().isLiquid();
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public AABB[] getBoxes(final HoriPlayer player) {
        Block b = data.getBlock();
        if (b instanceof BlockCarpet) {
            AABB[] boxArr = new AABB[1];
            boxArr[0] = new AABB(x, y, z, x + 1, y + 0.0625, z + 1);
            return boxArr;
        } else if (b instanceof BlockSnow && data.get(BlockSnow.LAYERS) == 1) {
            AABB[] boxArr = new AABB[1];
            boxArr[0] = new AABB(x, y, z, x + 1, y, z + 1);
            return boxArr;
        } else if (b instanceof BlockSoil) {
            if (player.protocol == 47) {
                AABB[] boxArr = new AABB[1];
                boxArr[0] = new AABB(x, y, z, x + 1, y + 1, z + 1);
                return boxArr;
            } else {
                AABB[] boxArr = new AABB[1];
                boxArr[0] = new AABB(x, y, z, x + 1, y + 0.9375, z + 1);
                return boxArr;
            }
        } else if (b instanceof BlockWaterLily) {
            if (player.protocol == 47) {
                AABB[] boxArr = new AABB[1];
                boxArr[0] = new AABB(x, y, z, x + 1, y + 0.015625, z + 1);
                return boxArr;
            } else {
                AABB[] boxArr = new AABB[1];
                boxArr[0] = new AABB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.09375, z + 0.9375);
                return boxArr;
            }
        }

        // Have to update shape
        b.updateShape(world, bPos);
        List<AxisAlignedBB> bbs = new ArrayList<>();
        AxisAlignedBB cube = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        b.a(world, bPos, data, cube, bbs, null);

        AxisAlignedBB[] raw = bbs.toArray(new AxisAlignedBB[0]);
        AABB[] boxes = new AABB[bbs.size()];

        for (int i = 0; i < bbs.size(); i++) {
            boxes[i] = new AABB(raw[i].a, raw[i].b, raw[i].c, raw[i].d, raw[i].e, raw[i].f);
        }

        return boxes;
    }

    @Override
    public float getFriction() {
        return data.getBlock().frictionFactor;
    }

    @Override
    public Location getPos() {
        return new Location(world.getWorld(), x, y, z);
    }

    @Override
    public Vector3D getFlowDirection() {
        Vector3D vec = new Vector3D();

        Vec3D nmsVec = new Vec3D(0, 0, 0);
        if (!this.isLiquid()) {
            return vec;
        }

        if (!world.areChunksLoaded(bPos, 1)) {
            return vec;
        }

        nmsVec = data.getBlock().a(world, bPos, (Entity) null, nmsVec);
        vec.setX(nmsVec.a);
        vec.setY(nmsVec.b);
        vec.setZ(nmsVec.c);
        return vec;
    }
}