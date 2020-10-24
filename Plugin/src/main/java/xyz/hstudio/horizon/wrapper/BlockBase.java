package xyz.hstudio.horizon.wrapper;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.util.AABB;

import java.util.ArrayList;
import java.util.List;

public class BlockBase {

    protected final WorldBase world;
    protected final IBlockData data;
    protected final BlockPosition bPos;
    protected final Material type;

    public BlockBase(WorldBase world, IBlockData data, int x, int y, int z) {
        this.world = world;
        this.data = data;
        this.bPos = new BlockPosition(x, y, z);
        this.type = CraftMagicNumbers.getMaterial(this.data.getBlock());
    }

    public Material type() {
        return type;
    }

    public boolean isSolid() {
        return data.getBlock().getMaterial().isSolid();
    }

    public boolean isLiquid() {
        return data.getBlock().getMaterial().isLiquid();
    }

    public float hardness() {
        return data.getBlock().g(world.worldServer, bPos);
    }

    public float friction() {
        return data.getBlock().frictionFactor;
    }

    public AABB[] boxes(HPlayer p) {
        Block b = data.getBlock();
        double x = bPos.getX(), y = bPos.getY(), z = bPos.getZ();
        if (b instanceof BlockCarpet) {
            AABB[] boxes = new AABB[1];
            boxes[0] = new AABB(x, y, z, x + 1, y + 0.0625, z + 1);
            return boxes;
        } else if (b instanceof BlockSnow && data.get(BlockSnow.LAYERS) == 1) {
            AABB[] boxes = new AABB[1];
            boxes[0] = new AABB(x, y, z, x + 1, y, z + 1);
            return boxes;
        } else if (b instanceof BlockSoil) {
            AABB[] boxes = new AABB[1];
            if (p.protocol == 47) {
                boxes[0] = new AABB(x, y, z, x + 1, y + 1, z + 1);
            } else {
                boxes[0] = new AABB(x, y, z, x + 1, y + 0.9375, z + 1);
            }
            return boxes;
        } else if (b instanceof BlockWaterLily) {
            AABB[] boxes = new AABB[1];
            if (p.protocol == 47) {
                boxes[0] = new AABB(x, y, z, x + 1, y + 0.015625, z + 1);
            } else {
                boxes[0] = new AABB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.09375, z + 0.9375);
            }
            return boxes;
        } else if (b instanceof BlockLadder) {
            AABB[] boxes = new AABB[1];
            if (p.protocol == 47) {
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

        b.updateShape(world.worldServer, bPos);
        List<AxisAlignedBB> bbs = new ArrayList<>();
        AxisAlignedBB cube = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
        b.a(world.worldServer, bPos, data, cube, bbs, null);

        AxisAlignedBB[] raw = bbs.toArray(new AxisAlignedBB[0]);
        AABB[] boxes = new AABB[raw.length];

        for (int i = 0; i < bbs.size(); i++) {
            boxes[i] = new AABB(raw[i].a, raw[i].b, raw[i].c, raw[i].d, raw[i].e, raw[i].f);
        }

        return boxes;
    }
}