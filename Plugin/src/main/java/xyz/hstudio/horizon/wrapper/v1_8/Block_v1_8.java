package xyz.hstudio.horizon.wrapper.v1_8;

import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import xyz.hstudio.horizon.wrapper.BlockBase;

public class Block_v1_8 extends BlockBase {

    private final World_v1_8 world;
    private final IBlockData data;
    private final Material type;

    public Block_v1_8(World_v1_8 world, IBlockData data) {
        this.world = world;
        this.data = data;
        this.type = CraftMagicNumbers.getMaterial(this.data.getBlock());
    }

    @Override
    public Material getType() {
        return type;
    }

    @Override
    public boolean isSolid() {
        return data.getBlock().getMaterial().isSolid();
    }
}