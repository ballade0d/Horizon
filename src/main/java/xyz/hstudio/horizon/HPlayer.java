package xyz.hstudio.horizon;

import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.outbound.AttributeEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.module.checks.*;
import xyz.hstudio.horizon.network.PacketHandler;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.EntityWrapper;
import xyz.hstudio.horizon.wrapper.ItemWrapper;
import xyz.hstudio.horizon.wrapper.WorldWrapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static xyz.hstudio.horizon.api.enums.Detection.*;

public class HPlayer {

    private static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    public final List<AttributeEvent.AttributeModifier> moveFactors = new CopyOnWriteArrayList<>();
    public final Queue<Pair<Integer, Long>> pings = new ConcurrentLinkedQueue<>();
    public final Map<Integer, Runnable> simulatedCmds = new ConcurrentHashMap<>();

    public final EntityPlayer nms;
    public final EntityWrapper base;
    public final int protocol;
    public final ChannelPipeline pipeline;
    public final PacketHandler packetHandler;
    public final Inventory inventory;
    public final Physics physics;
    public final Status status;
    public final Velocity velocity;
    public final Teleport teleport;
    public final Map<Detection, CheckBase> checks;

    public int currTick;

    public HPlayer(Player shit) {
        this.nms = ((CraftPlayer) shit).getHandle();
        this.base = new EntityWrapper(nms);
        this.protocol = 47; // TODO: Finish this
        this.checks = Collections.unmodifiableMap(new EnumMap<Detection, CheckBase>(Detection.class) {
            {
                put(AIM_ASSIST, new AimAssist(HPlayer.this));
                put(ANTI_VELOCITY, new AntiVelocity(HPlayer.this));
                put(BAD_PACKETS, new BadPackets(HPlayer.this));
                put(FAST_BREAK, new FastBreak(HPlayer.this));
                put(GROUND_SPOOF, new GroundSpoof(HPlayer.this));
                put(HEALTH_TAG, new HealthTag(HPlayer.this));
                put(HIT_BOX, new HitBox(HPlayer.this));
                put(KILL_AURA, new KillAura(HPlayer.this));
                put(KILL_AURA_BOT, new KillAuraBot(HPlayer.this));
                put(NO_SWING, new NoSwing(HPlayer.this));
                put(PHASE, new Phase(HPlayer.this));
                put(VERTICAL_MOVEMENT, new VerticalMovement(HPlayer.this));
            }
        });

        this.pipeline = nms.playerConnection.networkManager.channel.pipeline();
        this.packetHandler = new PacketHandler(this);

        this.inventory = new Inventory();
        this.physics = new Physics();
        this.status = new Status();
        this.velocity = new Velocity();
        this.teleport = new Teleport();

        inst.getPlayers().put(nms.getUniqueID(), this);
    }

    public void sendMessage(String message) {
        nms.sendMessage(CraftChatMessage.fromString(message));
    }

    public WorldWrapper world() {
        return new WorldWrapper(nms.getWorld());
    }

    public boolean teleportUnsafe(Location loc) {
        return nms.getBukkitEntity().teleport(loc.bukkit());
    }

    public float moveFactor() {
        float value = 0.1f;
        for (AttributeEvent.AttributeModifier modifier : moveFactors) {
            switch (modifier.operation) {
                case 0:
                    value += modifier.value;
                    continue;
                case 1:
                    value += 0.1f * modifier.value;
                    continue;
                case 2:
                    value += value * modifier.value;
                    continue;
                default:
            }
        }
        return value;
    }

    public WorldSettings.EnumGamemode getMode() {
        return nms.playerInteractManager.getGameMode();
    }

    /**
     * Run a command after (player's ping) milliseconds,
     * usually be used to make sure if a packet sent by the server is received by the client
     *
     * @param runnable Command
     */
    public void sendSimulatedAction(Runnable runnable) {
        // I don't think this can be duplicated
        int rand = ThreadLocalRandom.current().nextInt();
        simulatedCmds.put(rand, runnable);
        pipeline.writeAndFlush(new PacketPlayOutKeepAlive(rand));
    }

    public boolean executeSimulatedAction(int id) {
        Runnable runnable = simulatedCmds.get(id);
        if (runnable == null) {
            return false;
        }
        runnable.run();
        simulatedCmds.remove(id);
        return true;
    }

    public float flySpeed() {
        return nms.abilities.flySpeed * 2.0F;
    }

    public float walkSpeed() {
        return nms.abilities.walkSpeed * 2.0F;
    }

    public int getEffectAmplifier(MobEffectList type) {
        for (MobEffect effect : nms.getEffects()) {
            if (effect.getEffectId() != type.id) {
                continue;
            }
            return effect.getAmplifier() + 1;
        }
        return 0;
    }

    public class Inventory {

        public int heldSlot;

        public Inventory() {
            this.heldSlot = nms.inventory.itemInHandIndex;
        }

        public ItemWrapper hand() {
            return new ItemWrapper(nms.inventory.items[heldSlot]);
        }

        public boolean contains(org.bukkit.Material type) {
            return Arrays.stream(nms.inventory.items)
                    .anyMatch(i -> Item.getId(i.getItem()) == type.getId());
        }
    }

    public class Physics {

        public Location position;
        public boolean onGround;
        public boolean onGroundReally;
        public Vector3D oldVelocity;
        public Vector3D velocity;
        public float friction;
        public Set<Direction> touchedFaces = Collections.emptySet();

        public Physics() {
            this.position = base.position();
            this.oldVelocity = new Vector3D(0, 0, 0);
            this.velocity = new Vector3D(0, 0, 0);
        }

        public Vector3D headPos() {
            Vector3D adder = new Vector3D(0, status.isSneaking ? 1.54 : 1.62, 0);
            return position.plus(adder);
        }
    }

    public class Status {

        public int ping;
        public boolean isSneaking;
        public boolean isSprinting;
        public boolean hitSlowdown;
        public boolean teleport;

        public boolean isEating, isPullingBow, isBlocking;

        public Status() {
            this.ping = nms.ping;
            this.isSneaking = nms.isSneaking();
            this.isSprinting = nms.isSprinting();
        }
    }

    public class Velocity {

        public float x, y, z;
        public int receivedTick;
    }

    public class Teleport {

        public boolean teleporting;
        public Location location;
    }
}