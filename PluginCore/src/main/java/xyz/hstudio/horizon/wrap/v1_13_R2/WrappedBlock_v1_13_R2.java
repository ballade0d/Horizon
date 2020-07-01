package xyz.hstudio.horizon.wrap.v1_13_R2;

import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.ArrayList;
import java.util.List;

public class WrappedBlock_v1_13_R2 implements IWrappedBlock {

    private final WorldServer world;
    private final BlockPosition bPos;
    private final IBlockData data;
    private final Material type;
    private final int x, y, z;

    public WrappedBlock_v1_13_R2(final org.bukkit.block.Block block) {
        this.world = ((CraftWorld) block.getWorld()).getHandle();
        this.bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        this.data = world.getType(bPos);
        this.type = CraftMagicNumbers.getMaterial(data.getBlock());
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
    }

    public WrappedBlock_v1_13_R2(final org.bukkit.World world, final BlockPosition bPos, final IBlockData data) {
        this.world = ((CraftWorld) world).getHandle();
        this.bPos = bPos;
        this.data = data;
        this.type = CraftMagicNumbers.getMaterial(data.getBlock());
        this.x = bPos.getX();
        this.y = bPos.getY();
        this.z = bPos.getZ();
    }

    @Override
    public Material getType() {
        return this.type;
    }

    @Override
    public boolean isSolid() {
        return this.data.getMaterial().isSolid();
    }

    @Override
    public boolean isLiquid() {
        return this.data.getMaterial().isLiquid();
    }

    @Override
    public boolean isOccluding() {
        return this.data.isOccluding();
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
        } else if (b instanceof BlockLadder) {
            if (player.protocol == 47) {
                AABB[] boxArr = new AABB[1];
                switch (data.get(BlockLadder.FACING)) {
                    case NORTH:
                        boxArr[0] = new AABB(0, 0, 0.875, 1, 1, 1);
                        break;
                    case SOUTH:
                        boxArr[0] = new AABB(0, 0, 0, 1, 1, 0.125);
                        break;
                    case WEST:
                        boxArr[0] = new AABB(0.875, 0, 0, 1, 1, 1);
                        break;
                    case EAST:
                    default:
                        boxArr[0] = new AABB(0, 0, 0, 0.125, 1, 1);
                        break;
                }
                return boxArr;
            } else {
                AABB[] boxArr = new AABB[1];
                switch (data.get(BlockLadder.FACING)) {
                    case NORTH:
                        boxArr[0] = new AABB(0, 0, 0.8125, 1, 1, 1);
                        break;
                    case SOUTH:
                        boxArr[0] = new AABB(0, 0, 0, 1, 1, 0.1875);
                        break;
                    case WEST:
                        boxArr[0] = new AABB(0.8125, 0, 0, 1, 1, 1);
                        break;
                    case EAST:
                    default:
                        boxArr[0] = new AABB(0, 0, 0, 0.1875, 1, 1);
                        break;
                }
                return boxArr;
            }
        }

        VoxelShape voxelShape = data.getCollisionShape(world, bPos);
        List<AxisAlignedBB> bbs = new ArrayList<>(voxelShape.d());

        AxisAlignedBB[] raw = bbs.toArray(new AxisAlignedBB[0]);
        AABB[] boxes = new AABB[bbs.size()];

        for (int i = 0; i < bbs.size(); i++) {
            boxes[i] = new AABB(raw[i].minX + x, raw[i].minY + y, raw[i].minZ + z, raw[i].maxX + x, raw[i].maxY + y, raw[i].maxZ + z);
        }

        return boxes;
    }

    @Override
    public float getFriction() {
        return data.getBlock().n();
    }

    @Override
    public Location getPos() {
        return new Location(world.getWorld(), x, y, z);
    }

    @Override
    public Vector3D getFlowDirection() {
        Vector3D vec = new Vector3D();

        Vec3D nmsVec;
        if (!data.getMaterial().isLiquid()) {
            return vec;
        }

        if (!world.areChunksLoaded(bPos, 1)) {
            return vec;
        }

        nmsVec = ((BlockFluids) data.getBlock()).h(data).a((IWorldReader) world, bPos);
        vec.setX(nmsVec.x);
        vec.setY(nmsVec.y);
        vec.setZ(nmsVec.z);
        return vec;
    }
}