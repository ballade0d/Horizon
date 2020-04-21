package xyz.hstudio.horizon.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class BlockUtils {

    private BlockUtils() {
    }

    private static final Set<Material> SOLID = EnumSet.noneOf(Material.class);
    public static final Set<Material> SHULKER_BOX = EnumSet.noneOf(Material.class);

    static {
        Set<Material> solid = new HashSet<>();
        solid.add(Material.SNOW);
        solid.add(Material.LADDER);
        solid.add(Material.VINE);
        solid.add(MatUtils.LILY_PAD.parse());
        solid.add(MatUtils.COCOA_BEANS.parse());
        solid.add(MatUtils.END_ROD.parse());
        solid.add(MatUtils.CHORUS_FLOWER.parse());
        solid.add(MatUtils.CHORUS_PLANT.parse());
        solid.add(MatUtils.SEA_PICKLE.parse());
        solid.add(MatUtils.FARMLAND.parse());
        solid.add(MatUtils.SCAFFOLDING.parse());
        solid.add(MatUtils.BAMBOO.parse());

        solid.add(MatUtils.REPEATER.parse());

        solid.add(Material.FLOWER_POT);

        for (Material material : Material.values()) {
            if (material.name().startsWith("LEGACY_")) {
                continue;
            }
            if (material.name().contains("COMPARATOR") || material.name().contains("DIODE")) {
                solid.add(material);
            }
            // For 1.13+ potted flower material
            if (material.name().contains("POTTED")) {
                solid.add(material);
            }
            // For 1.13+ skull
            if (material.isBlock() && (material.name().contains("SKULL") || material.name().contains("HEAD"))) {
                solid.add(material);
            }
            // For 1.13+ carpet
            if (material.name().contains("CARPET")) {
                solid.add(material);
            }
            // For 1.12+ shulker box
            if (material.name().contains("SHULKER_BOX")) {
                SHULKER_BOX.add(material);
            }
        }

        solid.removeIf(Objects::isNull);

        BlockUtils.SOLID.addAll(solid);
    }

    public static boolean isSolid(final IWrappedBlock block) {
        return block.isSolid() || SOLID.contains(block.getType());
    }

    public static boolean isSolid(final Material type) {
        return type.isSolid() || SOLID.contains(type);
    }

    public static Set<IWrappedBlock> getBlocksInLocation(final Location loc) {
        Set<IWrappedBlock> blocks = new HashSet<>();
        blocks.add(loc.add(0.3, 0, 0).getBlock());
        blocks.add(loc.add(0, 0, 0.3).getBlock());
        blocks.add(loc.add(-0.3, 0, 0).getBlock());
        blocks.add(loc.add(0, 0, -0.3).getBlock());
        blocks.add(loc.add(0.3, 0, 0.3).getBlock());
        blocks.add(loc.add(-0.3, 0, -0.3).getBlock());
        blocks.add(loc.add(0.3, 0, -0.3).getBlock());
        blocks.add(loc.add(-0.3, 0, 0.3).getBlock());
        blocks.remove(null);
        return blocks;
    }

    /**
     * Get player's touching block faces.
     *
     * @author Islandscout, MrCraftGoo
     */
    public static Set<BlockFace> checkTouchingBlock(final HoriPlayer player, final AABB box,
                                                    final World world, final double borderSize) {
        Vector3D min = new Vector3D(box.minX - borderSize, box.minY - borderSize, box.minZ - borderSize);
        Vector3D max = new Vector3D(box.maxX + borderSize, box.maxY + borderSize, box.maxZ + borderSize);
        Set<BlockFace> directions = EnumSet.noneOf(BlockFace.class);
        for (int x = (int) (min.x < 0 ? min.x - 1 : min.x); x <= max.x; x++) {
            for (int y = (int) min.y - 1; y <= max.y; y++) {
                for (int z = (int) (min.z < 0 ? min.z - 1 : min.z); z <= max.z; z++) {
                    IWrappedBlock b = new Location(world, x, y, z).getBlock();
                    if (b == null) {
                        continue;
                    }
                    for (AABB blockBox : b.getBoxes(player)) {
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