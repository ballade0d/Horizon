package xyz.hstudio.horizon.wrapper.v1_12;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.IBlockData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import xyz.hstudio.horizon.wrapper.BlockBase;

public class Block_v1_12 extends BlockBase {

    private final World_v1_12 world;
    private final IBlockData data;
    private final BlockPosition bPos;
    private final Material type;

    public Block_v1_12(World_v1_12 world, IBlockData data, int x, int y, int z) {
        this.world = world;
        this.data = data;
        this.bPos = new BlockPosition(x, y, z);
        this.type = CraftMagicNumbers.getMaterial(this.data.getBlock());
    }

    @Override
    public Material type() {
        return type;
    }

    @Override
    public boolean isSolid() {
        return data.getMaterial().isSolid();
    }

    @Override
    public float hardness() {
        return data.getBlock().a(data, world.worldServer, bPos);
    }

    @Override
    public float friction() {
        return data.getBlock().frictionFactor;
    }
}