package xyz.hstudio.horizon.data;

import io.netty.channel.ChannelPipeline;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.checks.*;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.network.ChannelHandler;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HoriPlayer {

    public String lang;
    public boolean verbose;
    public final AntiVelocityData antiVelocityData = new AntiVelocityData();
    public final BadPacketData badPacketData = new BadPacketData();
    public final GroundSpoofData groundSpoofData = new GroundSpoofData();
    public final HitBoxData hitBoxData = new HitBoxData();
    public final InvalidMotionData invalidMotionData = new InvalidMotionData();
    public final InventoryData inventoryData = new InventoryData();
    public final KillAuraData killAuraData = new KillAuraData();
    public final NoSwingData noSwingData = new NoSwingData();
    public final ScaffoldData scaffoldData = new ScaffoldData();
    public final SpeedData speedData = new SpeedData();
    public final TimerData timerData = new TimerData();
    public Player player;
    public long currentTick;
    public World world;
    public Location position;
    public int heldSlot;
    public float friction;
    public double prevPrevDeltaY;
    public Vector3D velocity = new Vector3D(0, 0, 0);
    public List<Pair<Vector3D, Long>> velocities = new LinkedList<>();
    public Map<Location, Long> clientBlocks = new ConcurrentHashMap<>();
    public List<AABB> piston = new ArrayList<>();
    public Set<BlockFace> touchingFaces = EnumSet.noneOf(BlockFace.class);
    public boolean isSneaking;
    public boolean isSprinting;
    public boolean isEating;
    public boolean isPullingBow;
    public boolean isOnGround;
    public boolean onGroundReally;
    public boolean isGliding;
    public boolean isInLiquid;
    public long lastTeleportAcceptTick = -1;
    public long toggleFlyTime;
    public long hitSlowdownTick = -1;
    public long teleportTime = -1;
    public boolean isTeleporting;
    public Location teleportPos;
    public int vehicle = -1;
    public long ping;
    public long lastRequestSent;
    public float moveFactor = 0.1F;
    public ChannelPipeline pipeline;

    public HoriPlayer(final Player player) {
        this.player = player;
        this.pipeline = McAccessor.INSTANCE.getPipeline(player);

        this.lang = Horizon.getInst().config.personalized_themes_default_lang;

        this.world = player.getWorld();
        this.position = new Location(player.getLocation());
        this.heldSlot = player.getInventory().getHeldItemSlot();

        ChannelHandler.register(this, this.pipeline);

        Horizon.PLAYERS.put(player.getUniqueId(), this);
    }

    public void addClientBlock(final Location location, final long initTick) {
        if (clientBlocks.size() >= 12) {
            return;
        }
        clientBlocks.put(location, initTick);
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
    public Vector3D getHeadPosition() {
        if (this.getVehicle() != null) {
            return position.toVector().setY(position.y + player.getEyeHeight());
        }
        Vector3D add = new Vector3D(0, this.isSneaking ? 1.54 : 1.62, 0);
        return this.position.toVector().add(add);
    }

    public void teleport(final Location location) {
        this.player.teleport(new org.bukkit.Location(location.world, location.x, location.y, location.z, location.yaw, location.pitch));
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
     * Get the level of an active enchantment
     */
    public int getEnchantmentEffectAmplifier(final String name) {
        for (ItemStack item : this.player.getInventory().getArmorContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            for (Map.Entry<Enchantment, Integer> enchantment : item.getEnchantments().entrySet()) {
                if (enchantment.getKey().getName().equals(name)) {
                    return enchantment.getValue();
                }
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
        McAccessor.INSTANCE.ensureMainThread(() -> this.pipeline.writeAndFlush(packet));
    }

    public LangFile getLang() {
        return Horizon.getInst().getLang(this.lang);
    }
}