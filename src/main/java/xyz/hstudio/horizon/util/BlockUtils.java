package xyz.hstudio.horizon.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

    public static boolean isSolid(final Block block) {
        return McAccessor.INSTANCE.isSolid(block);
    }

    /*
    public static boolean isSolid(final Material material) {
        boolean isSolid = material.isSolid();
        if (material == Material.SNOW ||
                material == MatUtils.LADDER.parse() ||
                material == MatUtils.VINE.parse() ||
                material == MatUtils.REPEATER.parse() ||
                material == MatUtils.DIODE_BLOCK_ON.parse() ||
                material == MatUtils.DIODE_BLOCK_OFF.parse() ||
                material == MatUtils.COMPARATOR.parse() ||
                material == MatUtils.REDSTONE_COMPARATOR_ON.parse() ||
                material == MatUtils.REDSTONE_COMPARATOR_OFF.parse() ||
                material == MatUtils.LILY_PAD.parse() ||
                material == MatUtils.COCOA_BEANS.parse() ||
                material == MatUtils.SEA_PICKLE.parse() ||
                material == MatUtils.FARMLAND.parse() ||
                material == MatUtils.END_ROD.parse() ||
                material == MatUtils.CHORUS_FLOWER.parse() ||
                material == MatUtils.CHORUS_PLANT.parse() ||
                material == MatUtils.SCAFFOLDING.parse() ||
                material == MatUtils.BAMBOO.parse() ||
                // org.bukkit.Material#isSolid is fake and shit
                material.name().contains("SKULL") ||
                material.name().contains("HEAD") ||
                material.name().contains("POT") ||
                material.name().contains("CARPET")) {
            isSolid = true;
        }
        return isSolid;
    }
     */

    public static List<Block> getBlocksInLocation(final Location loc) {
        List<Block> blocks = new ArrayList<>(8);
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

    /**
     * Get player's touching block faces.
     *
     * @author Islandscout, MrCraftGoo
     */
    public static Set<BlockFace> checkTouchingBlock(final AABB box, final World world, final double borderSize) {
        Vector3D min = new Vector3D(box.minX - borderSize, box.minY - borderSize, box.minZ - borderSize);
        Vector3D max = new Vector3D(box.maxX + borderSize, box.maxY + borderSize, box.maxZ + borderSize);
        Set<BlockFace> directions = EnumSet.noneOf(BlockFace.class);
        for (int x = (int) (min.x < 0 ? min.x - 1 : min.x); x <= max.x; x++) {
            for (int y = (int) min.y - 1; y <= max.y; y++) {
                for (int z = (int) (min.z < 0 ? min.z - 1 : min.z); z <= max.z; z++) {
                    Block b = new Location(world, x, y, z).getBlock();
                    if (b == null) {
                        continue;
                    }
                    for (AABB blockBox : McAccessor.INSTANCE.getBoxes(b)) {
                        if (blockBox.minX > box.maxX && blockBox.minX < max.x) {
                            directions.add(BlockFace.EAST);
                        }
                        if (blockBox.minY > box.maxY && blockBox.minY < max.y) {
                            directions.add(BlockFace.UP);
                        }
                        if (blockBox.minZ > box.maxZ && blockBox.minZ < max.z) {
                            directions.add(BlockFace.SOUTH);
                        }
                        if (blockBox.maxX > min.x && blockBox.maxX < box.minX) {
                            directions.add(BlockFace.WEST);
                        }
                        if (blockBox.maxY > min.y && blockBox.maxY < box.minY) {
                            directions.add(BlockFace.DOWN);
                        }
                        if (blockBox.maxZ > min.z && blockBox.maxZ < box.minZ) {
                            directions.add(BlockFace.NORTH);
                        }
                    }
                }
            }
        }
        return directions;
    }
}