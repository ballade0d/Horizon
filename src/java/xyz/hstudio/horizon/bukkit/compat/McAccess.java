package xyz.hstudio.horizon.bukkit.compat;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.bukkit.compat.v1_12_R1.McAccess_v1_12_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_13_R2.McAccess_v1_13_R2;
import xyz.hstudio.horizon.bukkit.compat.v1_14_R1.McAccess_v1_14_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_15_R1.McAccess_v1_15_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_8_R3.McAccess_v1_8_R3;
import xyz.hstudio.horizon.bukkit.util.AxisAlignedBB;
import xyz.hstudio.horizon.bukkit.util.Version;

public abstract class McAccess {

    @Getter
    private static McAccess inst;

    public static void init() {
        switch (Version.VERSION) {
            case v1_8_R3:
                inst = new McAccess_v1_8_R3();
                break;
            case v1_12_R1:
                inst = new McAccess_v1_12_R1();
                break;
            case v1_13_R2:
                inst = new McAccess_v1_13_R2();
                break;
            case v1_14_R1:
                inst = new McAccess_v1_14_R1();
                break;
            case v1_15_R1:
                inst = new McAccess_v1_15_R1();
                break;
        }
    }

    public abstract ChannelPipeline getPipeline(final Player player);

    /**
     * Fast sin method by using nms's ones.
     */
    public abstract float sin(final float v);

    /**
     * Fast cos method by using nms's ones.
     */
    public abstract float cos(final float v);

    /**
     * Check if a player has no hit cooldown.
     */
    public abstract boolean isAccumulated(final Player player);

    /**
     * Get the friction of a block.
     */
    public abstract float getFriction(final Block block);

    /**
     * Get the bounding box of a player.
     */
    public abstract AxisAlignedBB getCube(final Player player);

    /**
     * Run a task in the main thread.
     */
    public abstract void ensureMainThread(final Runnable task);

    /**
     * Get an entity in a specified world by id
     */
    public abstract Entity getEntity(final World world, final int id);
}