package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class BlockUtils {

    /**
     * Remember to use this method when getting block async.
     */
    public static Block getBlock(final Location loc) {
        if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
            return loc.getBlock();
        }
        return null;
    }
}