package xyz.hstudio.horizon.bukkit.data;

import io.netty.channel.ChannelPipeline;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.compat.McAccessor;
import xyz.hstudio.horizon.bukkit.data.checks.*;
import xyz.hstudio.horizon.bukkit.network.ChannelHandler;
import xyz.hstudio.horizon.bukkit.util.ClientBlock;
import xyz.hstudio.horizon.bukkit.util.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HoriPlayer {

    public final BadPacketData badPacketData = new BadPacketData();
    public final GroundSpoofData groundSpoofData = new GroundSpoofData();
    public final HitBoxData hitBoxData = new HitBoxData();
    public final InvalidMotionData invalidMotionData = new InvalidMotionData();
    public final InventoryData inventoryData = new InventoryData();
    public final KillAuraData killAuraData = new KillAuraData();
    public final ScaffoldData scaffoldData = new ScaffoldData();
    public final TimerData timerData = new TimerData();
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
    public Map<Location, ClientBlock> clientBlocks = new ConcurrentHashMap<>();
    public Set<BlockFace> touchingFaces = new HashSet<>();
    public boolean isSneaking;
    public boolean isSprinting;
    public boolean isEating;
    public boolean isPullingBow;
    public boolean isOnGround;
    public boolean onGroundReally;
    public boolean isGliding;
    public long toggleFlyTime;
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
        this.pipeline = McAccessor.INSTANCE.getPipeline(player);

        this.world = player.getWorld();
        this.position = new Location(player.getLocation());
        this.heldSlot = player.getInventory().getHeldItemSlot();

        ChannelHandler.register(this, this.pipeline);

        Horizon.PLAYERS.put(player.getUniqueId(), this);
    }

    public void addClientBlock(final Location location, final ClientBlock clientBlock) {
        if (clientBlocks.size() >= 12) {
            return;
        }
        clientBlocks.put(location, clientBlock);
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
     * Check if player is flying
     */
    public boolean isFlying() {
        return System.currentTimeMillis() - this.toggleFlyTime <= 100L || this.player.isFlying();
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
        McAccessor.INSTANCE.ensureMainThread(() -> this.player.sendMessage(msg));
    }

    /**
     * Get player's current vehicle.
     */
    public Entity getVehicle() {
        if (this.vehicle == -1) {
            return null;
        }
        return McAccessor.INSTANCE.getEntity(this.world, this.vehicle);
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
     * Send transaction request to the client.
     */
    public void sendRequest() {
        this.lastRequestSent = System.currentTimeMillis();
        this.sendPacket(McAccessor.INSTANCE.newTransactionPacket());
    }

    /**
     * Send a packet to the player
     */
    public void sendPacket(final Object packet) {
        this.pipeline.writeAndFlush(packet);
    }
}