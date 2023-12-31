package xyz.hstudio.horizon.wrapper;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.util.AABB;

import java.util.ArrayList;
import java.util.List;

public class BlockWrapper {

    protected final WorldWrapper world;
    protected final IBlockData data;
    protected final Block block;
    protected final BlockPosition bPos;
    protected final Material type;

    public BlockWrapper(WorldWrapper world, IBlockData data, BlockPosition bPos) {
        this.world = world;
        this.data = data;
        this.block = data.getBlock();
        this.bPos = bPos;
        this.type = CraftMagicNumbers.getMaterial(block);
        data.getBlock().updateShape(world.worldServer, bPos);
    }

    public Material type() {
        return type;
    }

    public boolean isSolid() {
        return block.getMaterial().isSolid();
    }

    public boolean isLiquid() {
        return block.getMaterial().isLiquid();
    }

    public boolean isAlwaysDestroyable() {
        return block.getMaterial().isAlwaysDestroyable();
    }

    public float hardness() {
        return block.g(world.worldServer, bPos);
    }

    public float friction() {
        return block.frictionFactor;
    }

    public AABB[] boxes(HPlayer p) {
        double x = bPos.getX(), y = bPos.getY(), z = bPos.getZ();
        if (block instanceof BlockCarpet) {
            AABB[] boxes = new AABB[1];
            boxes[0] = new AABB(x, y, z, x + 1, y + 0.0625, z + 1);
            return boxes;
        } else if (block instanceof BlockSnow && data.get(BlockSnow.LAYERS) == 1) {
            AABB[] boxes = new AABB[1];
            boxes[0] = new AABB(x, y, z, x + 1, y, z + 1);
            return boxes;
        } else if (block instanceof BlockSoil) {
            AABB[] boxes = new AABB[1];
            if (p.protocol <= 47) {
                boxes[0] = new AABB(x, y, z, x + 1, y + 1, z + 1);
            } else {
                boxes[0] = new AABB(x, y, z, x + 1, y + 0.9375, z + 1);
            }
            return boxes;
        } else if (block instanceof BlockWaterLily) {
            AABB[] boxes = new AABB[1];
            if (p.protocol <= 47) {
                boxes[0] = new AABB(x, y, z, x + 1, y + 0.015625, z + 1);
            } else {
                boxes[0] = new AABB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.09375, z + 0.9375);
            }
            return boxes;
        } else if (block instanceof BlockLadder) {
            AABB[] boxes = new AABB[1];
            if (p.protocol <= 47) {
                switch (data.get(BlockLadder.FACING)) {
                    case NORTH:
                        boxes[0] = new AABB(x, y, z + 0.875, x + 1, y + 1, z + 1);
                        break;
                    case SOUTH:
                        boxes[0] = new AABB(x, y, z, x + 1, y + 1, z + 0.125);
                        break;
                    case WEST:
                        boxes[0] = new AABB(x + 0.875, y, z, x + 1, y + 1, z + 1);
                        break;
                    case EAST:
                    default:
                        boxes[0] = new AABB(x, y, z, x + 0.125, y + 1, z + 1);
                        break;
                }
            } else {
                switch (data.get(BlockLadder.FACING)) {
                    case NORTH:
                        boxes[0] = new AABB(x, y, z + 0.8125, x + 1, y + 1, z + 1);
                        break;
                    case SOUTH:
                        boxes[0] = new AABB(x, y, z, x + 1, y + 1, z + 0.1875);
                        break;
                    case WEST:
                        boxes[0] = new AABB(x + 0.8125, y, z, x + 1, y + 1, z + 1);
                        break;
                    case EAST:
                    default:
                        boxes[0] = new AABB(x, y, z, x + 0.1875, y + 1, z + 1);
                        break;
                }
            }
            return boxes;
        }

        List<AxisAlignedBB> bbs = new ArrayList<>();
        AxisAlignedBB cube = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        block.a(world.worldServer, bPos, data, cube, bbs, null);

        AxisAlignedBB[] raw = bbs.toArray(new AxisAlignedBB[0]);
        AABB[] boxes = new AABB[raw.length];

        for (int i = 0; i < bbs.size(); i++) {
            boxes[i] = new AABB(raw[i].a, raw[i].b, raw[i].c, raw[i].d, raw[i].e, raw[i].f);
        }

        return boxes;
    }

    public int getX() {
        return bPos.getX();
    }

    public int getY() {
        return bPos.getY();
    }

    public int getZ() {
        return bPos.getZ();
    }
}