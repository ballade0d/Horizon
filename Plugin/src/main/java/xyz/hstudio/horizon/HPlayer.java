package xyz.hstudio.horizon;

import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_8_R3.PacketPlayOutKeepAlive;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.outbound.AttributeEvent;
import xyz.hstudio.horizon.module.*;
import xyz.hstudio.horizon.network.PacketHandler;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static xyz.hstudio.horizon.api.enums.Detection.*;

public class HPlayer {

    private static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    public final List<Pair<Location, Long>> teleports = new CopyOnWriteArrayList<>();
    public final List<AttributeEvent.AttributeModifier> moveFactors = new CopyOnWriteArrayList<>();
    public final Queue<Pair<Integer, Long>> pings = new ConcurrentLinkedQueue<>();
    public final Map<Integer, Runnable> simulatedCmds = new ConcurrentHashMap<>();

    public final CraftPlayer bukkit;
    public final EntityBase base;
    public final int protocol;
    public final ChannelPipeline pipeline;
    public final PacketHandler packetHandler;
    public final Inventory inventory;
    public final Physics physics;
    public final Status status;
    public final Velocity velocity;
    private final Map<Detection, CheckBase> checks;

    public HPlayer(Player bukkit) {
        this.bukkit = (CraftPlayer) bukkit;
        this.base = new EntityBase(bukkit);
        this.protocol = 0; // TODO: Finish this
        this.checks = new EnumMap<Detection, CheckBase>(Detection.class) {
            {
                put(AIM_ASSIST, new AimAssist(HPlayer.this));
                put(GROUND_SPOOF, new GroundSpoof(HPlayer.this));
                put(HIT_BOX, new HitBox(HPlayer.this));
                put(VERTICAL_MOVEMENT, new VerticalMovement(HPlayer.this));
            }
        };

        this.pipeline = this.bukkit.getHandle().playerConnection.networkManager.channel.pipeline();
        this.packetHandler = new PacketHandler(this);

        this.inventory = new Inventory();
        this.physics = new Physics();
        this.status = new Status();
        this.velocity = new Velocity();

        inst.getPlayers().put(bukkit.getUniqueId(), this);
    }

    public Map<Detection, CheckBase> getCheckMap() {
        return checks;
    }

    public Collection<CheckBase> getChecks() {
        return checks.values();
    }

    public WorldBase getWorld() {
        return new WorldBase(bukkit.getWorld());
    }

    public float moveFactor() {
        float value = 0.1f;
        for (AttributeEvent.AttributeModifier modifier : moveFactors) {
            switch (modifier.operation) {
                case 0:
                    value += modifier.value;
                    continue;
                case 1:
                case 2:
                    // WTF case 1 and 2 is the same???
                    value += value * modifier.value;
                    continue;
                default:
            }
        }
        return value;
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

    public int getPotionAmplifier(PotionEffectType type) {
        for (PotionEffect e : bukkit.getActivePotionEffects()) {
            // hashCode returns effect id
            if (e.getType().hashCode() != type.hashCode()) {
                continue;
            }
            return e.getAmplifier() + 1;
        }
        return 0;
    }

    public class Inventory {

        public int heldSlot;

        public Inventory() {
            this.heldSlot = bukkit.getInventory().getHeldItemSlot();
        }

        public ItemStack mainHand() {
            return bukkit.getInventory().getItem(heldSlot);
        }
    }

    public class Physics {

        public Location position;
        public boolean onGround;
        public boolean onGroundReally;
        public Vector3D prevVelocity;
        public Vector3D velocity;

        public Physics() {
            this.position = base.position();
            this.prevVelocity = new Vector3D(0, 0, 0);
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
        public boolean isTeleporting;

        public Status() {
            this.ping = bukkit.getHandle().ping;
            this.isSneaking = bukkit.isSneaking();
            this.isSprinting = bukkit.isSprinting();
        }
    }

    public class Velocity {

        public float x, y, z;
    }
}