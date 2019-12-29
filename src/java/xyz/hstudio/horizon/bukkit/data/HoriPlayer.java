package xyz.hstudio.horizon.bukkit.data;

import io.netty.channel.ChannelPipeline;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.data.checks.KillAuraData;
import xyz.hstudio.horizon.bukkit.data.checks.ScaffoldData;
import xyz.hstudio.horizon.bukkit.network.ChannelHandler;
import xyz.hstudio.horizon.bukkit.util.Location;

public class HoriPlayer {

    public final KillAuraData killAuraData = new KillAuraData();
    public final ScaffoldData scaffoldData = new ScaffoldData();
    public Player player;
    public long currentTick;
    public World world;
    public Location position;
    public int heldSlot;
    public float friction;
    public Vector velocity = new Vector(0, 0, 0);
    public boolean isSneaking;
    public boolean isSprinting;
    public boolean isOnGround;
    public long hitSlowdownTick = -1;
    private ChannelPipeline pipeline;

    public HoriPlayer(final Player player) {
        this.player = player;
        this.pipeline = McAccess.getInst().getPipeline(player);

        this.world = player.getWorld();
        this.position = new Location(player.getLocation());
        this.heldSlot = player.getInventory().getHeldItemSlot();

        ChannelHandler.register(this, this.pipeline);

        Horizon.PLAYERS.put(player.getUniqueId(), this);
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