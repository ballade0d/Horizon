package xyz.hstudio.horizon.compat;

import io.netty.channel.ChannelPipeline;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.inbound.MoveEvent;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;

public interface IMcAccessor {

    ChannelPipeline getPipeline(Player player);

    /**
     * Fast sin method by using nms's ones.
     */
    float sin(float v);

    /**
     * Fast cos method by using nms's ones.
     */
    float cos(float v);

    /**
     * Check if a player has no hit cooldown.
     */
    boolean isAccumulated(Player player);

    /**
     * Get the bounding box of an entity.
     */
    AABB getCube(Entity entity);

    /**
     * Run a task in the main thread.
     */
    void ensureMainThread(Runnable task);

    /**
     * Get the entity in a specified world by id
     */
    Entity getEntity(World world, int id);

    /**
     * Create a new transaction packet.
     */
    Object newTransactionPacket();

    /**
     * Stop a player from using item.
     */
    void releaseItem(Player player);

    /**
     * Check if player is colliding entities
     */
    boolean isCollidingEntities(World world, Player player, AABB aabb);

    /**
     * Set onGround statue of a move packet.
     */
    void setOnGround(MoveEvent e, boolean onGround);

    /**
     * Create an explosion packet
     */
    Object createExplosionPacket(double x, double y, double z);

    /**
     * Update a block with its data for player
     */
    void updateBlock(HoriPlayer player, Location loc);

    /**
     * Get the ping of a player calculated by server
     */
    int getPing(Player player);
}