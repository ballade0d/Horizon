package xyz.hstudio.horizon;

import io.netty.channel.ChannelPipeline;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.module.AimAssist;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.module.HitBox;
import xyz.hstudio.horizon.module.VerticalMovement;
import xyz.hstudio.horizon.network.PacketHandler;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static xyz.hstudio.horizon.api.enums.Detection.*;

public class HPlayer {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    public final List<Pair<Location, Integer>> teleports = new CopyOnWriteArrayList<>();

    public final Player bukkit;
    public final EntityBase base;
    public final int protocol;
    public final ChannelPipeline pipeline;
    public final PacketHandler packetHandler;
    public final AtomicInteger tick;
    public final Inventory inventory;
    public final Physics physics;
    public final Status status;
    private final Map<Detection, CheckBase> checks;

    public HPlayer(Player bukkit) {
        this.bukkit = bukkit;
        this.base = EntityBase.getEntity(bukkit);
        this.protocol = 0; // TODO: Finish this
        this.checks = new EnumMap<Detection, CheckBase>(Detection.class) {
            {
                put(AIM_ASSIST, new AimAssist(HPlayer.this));
                put(HIT_BOX, new HitBox(HPlayer.this));
                put(VERTICAL_MOVEMENT, new VerticalMovement(HPlayer.this));
            }
        };

        this.pipeline = AccessorBase.getInst().getPipeline(this);
        this.packetHandler = new PacketHandler(this);

        this.tick = new AtomicInteger();

        this.inventory = new Inventory();
        this.physics = new Physics();
        this.status = new Status();

        inst.getPlayers().put(bukkit.getUniqueId(), this);
    }

    public Map<Detection, CheckBase> getCheckMap() {
        return checks;
    }

    public Collection<CheckBase> getChecks() {
        return checks.values();
    }

    public WorldBase getWorld() {
        return WorldBase.getWorld(bukkit.getWorld());
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
        public Vector3D velocity;

        public Physics() {
            this.position = base.position();
            this.velocity = new Vector3D(0, 0, 0);
        }

        public Vector3D headPos() {
            Vector3D adder = new Vector3D(0, status.isSneaking ? 1.54 : 1.62, 0);
            return position.plus(adder);
        }
    }

    public class Status {

        public boolean isSneaking;
        public boolean isSprinting;
        public boolean isTeleporting;
    }
}