package xyz.hstudio.horizon.compat;

import io.netty.channel.ChannelPipeline;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public interface IMcAccessor {

    ChannelPipeline getPipeline(final Player player);

    /**
     * Fast sin method by using nms's ones.
     */
    float sin(final float v);

    /**
     * Fast cos method by using nms's ones.
     */
    float cos(final float v);

    /**
     * Check if a player has no hit cooldown.
     */
    boolean isAccumulated(final Player player);

    /**
     * Get the friction of a block.
     */
    float getFriction(final Block block);

    /**
     * Get the bounding box of an entity.
     */
    AABB getCube(final Entity entity);

    /**
     * Run a task in the main thread.
     */
    void ensureMainThread(final Runnable task);

    /**
     * Get the entity in a specified world by id
     */
    Entity getEntity(final World world, final int id);

    /**
     * Get the voxel shapes of a block.
     */
    AABB[] getBoxes(final Block block);

    /**
     * Get the value of movement speed attribute.
     */
    double getMoveFactor(final Player player);

    /**
     * Create a new transaction packet.
     */
    Object newTransactionPacket();

    /**
     * Get the flow direction of liquid
     */
    Vector3D getFlowDirection(final Block block);

    /**
     * Stop a player from using item.
     */
    void releaseItem(final Player player);
}