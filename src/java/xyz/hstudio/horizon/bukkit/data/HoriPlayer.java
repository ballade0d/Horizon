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
import xyz.hstudio.horizon.bukkit.data.checks.*;
import xyz.hstudio.horizon.bukkit.network.ChannelHandler;
import xyz.hstudio.horizon.bukkit.util.Location;

import java.util.ArrayList;
import java.util.List;

public class HoriPlayer {

    public final AutoSwitchData autoSwitchData = new AutoSwitchData();
    public final BadPacketData badPacketData = new BadPacketData();
    public final HitBoxData hitBoxData = new HitBoxData();
    public final InvalidMotionData invalidMotionData = new InvalidMotionData();
    public final KillAuraData killAuraData = new KillAuraData();
    public final ScaffoldData scaffoldData = new ScaffoldData();
    public Player player;
    public long currentTick;
    public World world;
    public Location position;
    public int heldSlot;
    public float friction;
    public double prevDeltaY;
    public double prevPrevDeltaY;
    public Vector velocity = new Vector(0, 0, 0);
    public List<Vector> velocities = new ArrayList<>();
    public boolean isSneaking;
    public boolean isSprinting;
    public boolean isEating;
    public boolean isPullingBow;
    public boolean isOnGround;
    public boolean isGliding;
    public long hitSlowdownTick = -1;
    public long teleportTime = -1;
    public boolean isTeleporting;
    public Location teleportPos;
    public int vehicle = -1;
    public long ping;
    public long lastRequestSent;
    public ChannelPipeline pipeline;

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
        if (this.getVehicle() != null) {
            return position.toVector().setY(position.y + player.getEyeHeight());
        }
        Vector add = new Vector(0, this.isSneaking ? 1.54 : 1.62, 0);
        return this.position.toVector().add(add);
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
     * Get the level of an active potion.
     */
    public int getPotionEffectAmplifier(final String name) {
        for (PotionEffect e : this.player.getActivePotionEffects()) {
            if (e.getType().getName().equals(name)) {
                return e.getAmplifier() + 1;
            }
        }
        return 0;
    }

    /**
     * Send a packet to the player
     */
    public void sendPacket(final Object packet) {
        this.pipeline.writeAndFlush(packet);
    }
}