package xyz.hstudio.horizon.data;

import io.netty.channel.ChannelPipeline;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import protocolsupport.api.ProtocolSupportAPI;
import us.myles.ViaVersion.api.Via;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.checks.*;
import xyz.hstudio.horizon.events.outbound.AttributeEvent;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.network.ChannelHandler;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.enums.Version;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.ClientBlock;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HoriPlayer {

    public String lang;
    public boolean verbose;
    public boolean analysis;
    public final AntiVelocityData antiVelocityData = new AntiVelocityData();
    public final AutoClickerData autoClickerData = new AutoClickerData();
    public final BadPacketData badPacketData = new BadPacketData();
    public final GroundSpoofData groundSpoofData = new GroundSpoofData();
    public final HitBoxData hitBoxData = new HitBoxData();
    public final InvalidMotionData invalidMotionData = new InvalidMotionData();
    public final InventoryClickData inventoryClickData = new InventoryClickData();
    public final InventoryData inventoryData = new InventoryData();
    public final KillAuraBotData killAuraBotData = new KillAuraBotData();
    public final KillAuraData killAuraData = new KillAuraData();
    public final NoSwingData noSwingData = new NoSwingData();
    public final InteractData interactData = new InteractData();
    public final SpeedData speedData = new SpeedData();
    public final TimerData timerData = new TimerData();
    private final Map<Runnable, Long> simulatedCmds = new ConcurrentHashMap<>();
    public final int protocol;
    private final Player player;
    public long currentTick;
    public World world;
    public Location position;
    public int heldSlot;
    public int foodLevel;
    public float friction;
    public double prevPrevDeltaY;
    public Vector3D velocity = new Vector3D(0, 0, 0);
    public final List<Pair<Vector3D, Long[]>> velocities = new LinkedList<>();
    public Location teleportLoc;
    public long teleportTime;
    public final Map<Location, ClientBlock> clientBlocks = new ConcurrentHashMap<>();
    public List<AttributeEvent.AttributeModifier> moveModifiers = new ArrayList<>();
    public Set<AABB> piston = new ConcurrentSet<>();
    public Set<BlockFace> touchingFaces = EnumSet.noneOf(BlockFace.class);
    public Location prevClientBlock;
    public int clientBlockCount;
    public boolean isSneaking;
    public boolean isSprinting;
    public boolean isEating;
    public boolean isPullingBow;
    public boolean isBlocking;
    public boolean onGround;
    public boolean onGroundReally;
    public boolean isGliding;
    public boolean isInLiquidStrict;
    public boolean isInLiquid;
    public long lastTeleportAcceptTick = -1;
    public long toggleFlyTick;
    public long hitSlowdownTick = -1;
    public int vehicle = -1;
    public long leaveVehicleTick = -1;
    public ChannelPipeline pipeline;

    public HoriPlayer(final Player player) {
        Version viaVer = Horizon.getInst().useViaVer ?
                Version.getVersion(Via.getAPI().getPlayerVersion(player.getUniqueId())) : Version.VERSION;
        Version pVer = Horizon.getInst().usePSupport ?
                Version.getVersion(ProtocolSupportAPI.getProtocolVersion(player).getId()) : Version.VERSION;
        this.protocol = viaVer == Version.VERSION ? pVer.minProtocol : viaVer.minProtocol;
        this.player = player;
        this.pipeline = McAccessor.INSTANCE.getPipeline(player);

        this.lang = Horizon.getInst().config.default_lang;

        this.world = player.getWorld();
        this.position = new Location(player.getLocation());
        this.heldSlot = player.getInventory().getHeldItemSlot();
        this.foodLevel = player.getFoodLevel();

        ChannelHandler.register(this, this.pipeline);

        Horizon.PLAYERS.put(player.getUniqueId(), this);
    }

    public Player getPlayer() {
        return player;
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
        return getPlayer().getInventory().getItem(this.heldSlot);
    }

    /**
     * Check if player is flying
     */
    public boolean isFlying() {
        return this.currentTick - this.toggleFlyTick <= 5L || getPlayer().isFlying();
    }

    /**
     * Get player's head position accurately.
     *
     * @return Player's head position.
     */
    public Vector3D getHeadPosition() {
        if (this.getVehicle() != null) {
            return position.toVector().setY(position.y + getPlayer().getEyeHeight());
        }
        Vector3D add = new Vector3D(0, this.isSneaking ? 1.54 : 1.62, 0);
        return this.position.toVector().add(add);
    }

    public void teleport(final Location location) {
        getPlayer().teleport(new org.bukkit.Location(location.world, location.x, location.y, location.z, location.yaw, location.pitch));
    }

    public void sendMessage(final String msg) {
        McAccessor.INSTANCE.ensureMainThread(() -> getPlayer().sendMessage(msg));
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
        for (PotionEffect e : getPlayer().getActivePotionEffects()) {
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
        for (ItemStack item : getPlayer().getInventory().getArmorContents()) {
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

    public void sendSimulatedAction(final Runnable action) {
        this.simulatedCmds.put(action, System.currentTimeMillis());
    }

    public void tick(final long currentTick, final long currTime) {
        if (this.simulatedCmds.size() == 0) {
            return;
        }
        for (Map.Entry<Runnable, Long> entry : this.simulatedCmds.entrySet()) {
            if (currTime - entry.getValue() < McAccessor.INSTANCE.getPing(player)) {
                continue;
            }
            entry.getKey().run();
            this.simulatedCmds.remove(entry.getKey());
        }
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

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof HoriPlayer)) {
            return false;
        }
        HoriPlayer player = (HoriPlayer) object;
        return this.player.getUniqueId().equals(player.getPlayer().getUniqueId());
    }
}