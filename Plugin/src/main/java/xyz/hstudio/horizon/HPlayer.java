package xyz.hstudio.horizon;

import io.netty.channel.ChannelPipeline;
import lombok.Data;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.module.VerticalMovement;
import xyz.hstudio.horizon.network.PacketHandler;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.EnumMap;
import java.util.Map;

import static xyz.hstudio.horizon.api.enums.Detection.VERTICAL_MOVEMENT;

public class HPlayer {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    private final Player bukkit;
    private final EntityBase base;
    @Getter
    private final Map<Detection, CheckBase> checks;

    @Getter
    private final ChannelPipeline pipeline;
    @Getter
    private final PacketHandler packetHandler;

    @Getter
    private final Physics physics;
    @Getter
    private final Inventory inventory;

    public HPlayer(Player bukkit) {
        this.bukkit = bukkit;
        this.base = EntityBase.getEntity(bukkit);
        this.checks = new EnumMap<Detection, CheckBase>(Detection.class) {
            {
                put(VERTICAL_MOVEMENT, new VerticalMovement(HPlayer.this));
            }
        };

        this.pipeline = AccessorBase.getInst().getPipeline(this);
        this.packetHandler = new PacketHandler(this);

        this.physics = new Physics();
        this.inventory = new Inventory();

        inst.getPlayers().put(bukkit.getUniqueId(), this);
    }

    public Player bukkit() {
        return bukkit;
    }

    public EntityBase base() {
        return base;
    }

    public WorldBase getWorld() {
        return WorldBase.getWorld(bukkit.getWorld());
    }

    @Data
    public class Inventory {

        private int heldItemSlot;

        public Inventory() {
            this.heldItemSlot = bukkit.getInventory().getHeldItemSlot();
        }

        public ItemStack main() {
            return bukkit.getInventory().getItem(heldItemSlot);
        }
    }

    @Data
    public class Physics {

        private Location position;

        public Physics() {
            this.position = base.position();
        }
    }
}