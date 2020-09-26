package xyz.hstudio.horizon.wrapper.v1_8;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import xyz.hstudio.horizon.wrapper.BlockBase;

public class Block_v1_8 extends BlockBase {

    private final World_v1_8 world;
    private final IBlockData data;
    private final BlockPosition bPos;
    private final Material type;

    public Block_v1_8(World_v1_8 world, IBlockData data, int x, int y, int z) {
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
        return data.getBlock().getMaterial().isSolid();
    }

    @Override
    public float hardness() {
        return data.getBlock().g(world.worldServer, bPos);
    }

    @Override
    public float friction() {
        return data.getBlock().frictionFactor;
    }
}