package xyz.hstudio.horizon.util;

import org.bukkit.Material;
import xyz.hstudio.horizon.wrapper.BlockBase;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BlockUtils {

    private static final Set<Material> SOLID = EnumSet.noneOf(Material.class);

    static {
        Set<Material> solid = new HashSet<>();
        solid.add(Material.SNOW);
        solid.add(Material.LADDER);
        solid.add(Material.VINE);
        solid.add(Material.WATER_LILY);
        solid.add(Material.COCOA);
        solid.add(Material.CARPET);
        solid.add(Material.SKULL);
        solid.add(Material.FLOWER_POT);
        solid.add(Material.SOIL);

        for (Material material : Material.values()) {
            if (material.name().contains("COMPARATOR") || material.name().contains("DIODE")) solid.add(material);
        }

        solid.removeIf(Objects::isNull);

        BlockUtils.SOLID.addAll(solid);
    }

    public static boolean isSolid(BlockBase block) {
        return block.isSolid() || SOLID.contains(block.type());
    }

    public static boolean isSolid(Material type) {
        return type.isSolid() || SOLID.contains(type);
    }

    public static Set<BlockBase> getBlocksInLocation(Location loc) {
        Set<BlockBase> blocks = new HashSet<>();
        blocks.add(loc.plus(0.3, 0, 0).getBlock());
        blocks.add(loc.plus(0, 0, 0.3).getBlock());
        blocks.add(loc.plus(-0.3, 0, 0).getBlock());
        blocks.add(loc.plus(0, 0, -0.3).getBlock());
        blocks.add(loc.plus(0.3, 0, 0.3).getBlock());
        blocks.add(loc.plus(-0.3, 0, -0.3).getBlock());
        blocks.add(loc.plus(0.3, 0, -0.3).getBlock());
        blocks.add(loc.plus(-0.3, 0, 0.3).getBlock());
        blocks.remove(null);
        return blocks;
    }
}