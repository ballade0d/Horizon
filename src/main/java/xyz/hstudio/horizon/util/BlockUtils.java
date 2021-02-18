package xyz.hstudio.horizon.util;

import org.bukkit.Material;
import xyz.hstudio.horizon.wrapper.BlockWrapper;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class BlockUtils {

    private static final Set<Material> SOLID = EnumSet.noneOf(Material.class);

    static {
        SOLID.add(Material.SNOW);
        SOLID.add(Material.SNOW_BLOCK);
        SOLID.add(Material.LADDER);
        SOLID.add(Material.VINE);
        SOLID.add(Material.WATER_LILY);
        SOLID.add(Material.COCOA);
        SOLID.add(Material.CARPET);
        SOLID.add(Material.SKULL);
        SOLID.add(Material.FLOWER_POT);
        SOLID.add(Material.SOIL);

        for (Material material : Material.values()) {
            if (material.name().contains("COMPARATOR") || material.name().contains("DIODE")) {
                SOLID.add(material);
            }
        }
    }

    public static boolean isSolid(BlockWrapper block) {
        return block.isSolid() || SOLID.contains(block.type());
    }

    public static boolean isSolid(Material type) {
        return type.isSolid() || SOLID.contains(type);
    }

    public static Set<BlockWrapper> getBlocksInLocation(Location loc) {
        Set<BlockWrapper> blocks = new HashSet<>();
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

    public static boolean blockNearbyIsSolid(Location loc, boolean definition) {
        Set<BlockWrapper> sample = new HashSet<>();
        sample.add(loc.plus(0, 0, 1).getBlock());
        sample.add(loc.plus(1, 0, 1).getBlock());
        sample.add(loc.plus(1, 0, 0).getBlock());
        sample.add(loc.plus(1, 0, -1).getBlock());
        sample.add(loc.plus(0, 0, -1).getBlock());
        sample.add(loc.plus(-1, 0, -1).getBlock());
        sample.add(loc.plus(-1, 0, 0).getBlock());
        sample.add(loc.plus(-1, 0, 1).getBlock());
        for (BlockWrapper b : sample) {
            if (b == null) {
                continue;
            }
            if (definition && b.isSolid()) {
                return true;
            } else if (b.isSolid()) {
                return true;
            }
        }
        return false;
    }
}