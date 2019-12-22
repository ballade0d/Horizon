package xyz.hstudio.horizon.bukkit.data;

import io.netty.channel.ChannelPipeline;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.bukkit.compat.McAccess;

public class HoriPlayer {

    public Player player;
    public long currentTick;
    public World world;
    public Location position;
    public int heldSlot;
    public boolean isSneaking;
    public boolean isSprinting;
    public long hitSlowdownTick = -1;
    private ChannelPipeline pipeline;

    public HoriPlayer(final Player player) {
        this.player = player;
        this.pipeline = McAccess.getInst().getPipeline(player);

        this.world = player.getWorld();
        this.position = player.getLocation();
        this.heldSlot = player.getInventory().getHeldItemSlot();
    }

    /**
     * This is updated before bukkit (Inventory#getItemInMainHand)
     * to avoid some issues.
     *
     * @return The item in main hand.
     */
    public ItemStack getHeldItem() {
        return this.player.getInventory().getItem(this.heldSlot);
    }
}