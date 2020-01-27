package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {

    /**
     * Remember to use this method when getting block async.
     */
    public static Block getBlock(final Location loc) {
        if (loc.world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
            return loc.getBlockUnsafe();
        }
        return null;
    }

    public static boolean isSolid(final Material material) {
        boolean isSolid = material.isSolid();
        if (material == Material.SNOW ||
                material == MatUtils.LADDER.parse() ||
                material == MatUtils.VINE.parse() ||
                material == MatUtils.REPEATER.parse() ||
                material == MatUtils.COMPARATOR.parse() ||
                material == MatUtils.LILY_PAD.parse() ||
                material == MatUtils.COCOA_BEANS.parse() ||
                material == MatUtils.SEA_PICKLE.parse() ||
                material == MatUtils.FARMLAND.parse() ||
                material == MatUtils.END_ROD.parse() ||
                material == MatUtils.CHORUS_FLOWER.parse() ||
                material == MatUtils.CHORUS_PLANT.parse() ||
                material == MatUtils.SCAFFOLDING.parse() ||
                material == MatUtils.BAMBOO.parse() ||
                material.name().contains("DIODE") ||
                material.name().contains("SKULL") ||
                material.name().contains("HEAD") ||
                material.name().contains("POT") ||
                material.name().contains("CARPET")) {
            isSolid = true;
        }
        return isSolid;
    }

    public static List<Block> getBlocksInLocation(final Location loc) {
        List<Block> blocks = new ArrayList<>();
        blocks.add(loc.add(0.3, 0, 0).getBlock());
        blocks.add(loc.add(0, 0, 0.3).getBlock());
        blocks.add(loc.add(-0.3, 0, 0).getBlock());
        blocks.add(loc.add(0, 0, -0.3).getBlock());
        blocks.add(loc.add(0.3, 0, 0.3).getBlock());
        blocks.add(loc.add(-0.3, 0, -0.3).getBlock());
        blocks.add(loc.add(0.3, 0, -0.3).getBlock());
        blocks.add(loc.add(-0.3, 0, 0.3).getBlock());
        Block prevBlock = null;
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block currBlock = blocks.get(i);
            if (currBlock == null || currBlock.getType() == Material.AIR || currBlock.equals(prevBlock)) {
                blocks.remove(i);
            }
            prevBlock = currBlock;
        }
        return blocks;
    }
}