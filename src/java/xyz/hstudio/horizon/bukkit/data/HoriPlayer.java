package xyz.hstudio.horizon.bukkit.data;

import io.netty.channel.ChannelPipeline;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.data.checks.AutoSwitchData;
import xyz.hstudio.horizon.bukkit.data.checks.InvalidMotionData;
import xyz.hstudio.horizon.bukkit.data.checks.KillAuraData;
import xyz.hstudio.horizon.bukkit.data.checks.ScaffoldData;
import xyz.hstudio.horizon.bukkit.network.ChannelHandler;
import xyz.hstudio.horizon.bukkit.util.Location;

public class HoriPlayer {

    public final AutoSwitchData autoSwitchData = new AutoSwitchData();
    public final InvalidMotionData invalidMotionData = new InvalidMotionData();
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
    public boolean isGliding;
    public long hitSlowdownTick = -1;
    public int vehicle = -1;
    public long ping;
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

    /**
     * Get player's head position accurately.
     *
     * @return Player's head position.
     */
    public Vector getHeadPosition() {
        Vector add = new Vector(0, 0, 0);
        add.setY(this.isSneaking ? 1.54 : 1.62);
        return position.toVector().clone().add(add);
    }

    public void sendMessage(final String msg) {
        McAccess.getInst().ensureMainThread(() -> this.player.sendMessage(msg));
    }

    /**
     * Get player's current vehicle.
     */
    public Entity getVehicle() {
        if (this.vehicle == -1) {
            return null;
        }
        return McAccess.getInst().getEntity(this.world, this.vehicle);
    }

    /**
     * Get the level of a potion
     *
     * @param name
     * @return
     */
    public int getPotionEffectAmplifier(final String name) {
        for (PotionEffect e : this.player.getActivePotionEffects()) {
            if (e.getType().getName().equals(name)) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }
}