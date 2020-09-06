package xyz.hstudio.horizon;

import io.netty.channel.ChannelPipeline;
import lombok.Data;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.module.check.CheckBase;
import xyz.hstudio.horizon.network.PacketHandler;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.ArrayList;
import java.util.List;

public class HPlayer {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    @Getter
    private final Player bPlayer;
    @Getter
    private final EntityBase base;
    @Getter
    private final List<CheckBase> checks;

    @Getter
    private final ChannelPipeline pipeline;
    @Getter
    private final PacketHandler packetHandler;

    @Getter
    private final Physics physics;
    @Getter
    private final Inventory inventory;

    public HPlayer(Player bPlayer) {
        this.bPlayer = bPlayer;
        this.base = EntityBase.getEntity(bPlayer);
        this.checks = new ArrayList<>();

        this.pipeline = AccessorBase.getInst().getPipeline(this);
        this.packetHandler = new PacketHandler(this);

        this.physics = new Physics();
        this.inventory = new Inventory();

        inst.getPlayers().put(bPlayer.getUniqueId(), this);
    }

    public WorldBase getWorld() {
        return WorldBase.getWorld(bPlayer.getWorld());
    }

    @Data
    public class Inventory {

        private int heldItemSlot;

        public Inventory() {
            this.heldItemSlot = bPlayer.getInventory().getHeldItemSlot();
        }

        public ItemStack getItemInHand() {
            return bPlayer.getInventory().getItem(heldItemSlot);
        }
    }

    @Data
    public class Physics {

        private Location pos;

        public Physics() {
            this.pos = getBase().getPosition();
        }
    }
}