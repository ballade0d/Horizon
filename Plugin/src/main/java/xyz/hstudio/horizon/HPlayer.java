package xyz.hstudio.horizon;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.module.HitBox;
import xyz.hstudio.horizon.module.VerticalMovement;
import xyz.hstudio.horizon.network.PacketHandler;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.EnumMap;
import java.util.Map;

import static xyz.hstudio.horizon.api.enums.Detection.HIT_BOX;
import static xyz.hstudio.horizon.api.enums.Detection.VERTICAL_MOVEMENT;

public class HPlayer {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    private final Player bukkit;
    private final EntityBase base;
    @Getter
    private final int protocol;
    @Getter
    private final Map<Detection, CheckBase> checks;

    @Getter
    private final ChannelPipeline pipeline;
    @Getter
    private final PacketHandler packetHandler;

    private final Inventory inventory;
    private final Physics physics;
    private final Status status;

    public HPlayer(Player bukkit) {
        this.bukkit = bukkit;
        this.base = EntityBase.getEntity(bukkit);
        this.protocol = 0; // TODO: Finish this
        this.checks = new EnumMap<Detection, CheckBase>(Detection.class) {
            {
                put(HIT_BOX, new HitBox(HPlayer.this));
                put(VERTICAL_MOVEMENT, new VerticalMovement(HPlayer.this));
            }
        };

        this.pipeline = AccessorBase.getInst().getPipeline(this);
        this.packetHandler = new PacketHandler(this);

        this.inventory = new Inventory();
        this.physics = new Physics();
        this.status = new Status();

        inst.getPlayers().put(bukkit.getUniqueId(), this);
    }

    public Player bukkit() {
        return bukkit;
    }

    public EntityBase base() {
        return base;
    }

    public Inventory inventory() {
        return inventory;
    }

    public Physics physics() {
        return physics;
    }

    public Status status() {
        return status;
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
    }
}