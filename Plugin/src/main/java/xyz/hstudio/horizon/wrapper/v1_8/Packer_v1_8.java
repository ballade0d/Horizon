package xyz.hstudio.horizon.wrapper.v1_8;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.BlockDigEvent;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.ItemInteractEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockBase;
import xyz.hstudio.horizon.wrapper.PackerBase;

import java.io.IOException;

public class Packer_v1_8 extends PackerBase {

    private Event toEvent(HPlayer p, PacketPlayInAbilities packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(9));
        try {
            packet.b(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte status = serializer.readByte();

        boolean invulnerable = (status & 1) > 0;
        boolean flying = (status & 2) > 0;
        boolean canFly = (status & 4) > 0;
        boolean inCreative = (status & 8) > 0;
        float flyingSpeed = serializer.readFloat();
        float walkingSpeed = serializer.readFloat();

        // Event here
        return null;
    }

    private Event toEvent(HPlayer p, PacketPlayInArmAnimation packet) {
        long timestamp = packet.timestamp;
        // Event here
        return null;
    }

    private Event toEvent(HPlayer p, PacketPlayInBlockDig packet) {
        BlockPosition nmsPos = packet.a();
        EnumDirection nmsDir = packet.b();
        PacketPlayInBlockDig.EnumPlayerDigType nmsType = packet.c();

        BlockDigEvent.DigType digType = null;
        ItemInteractEvent.InteractType interactType = null;
        switch (nmsType) {
            case START_DESTROY_BLOCK:
                digType = BlockDigEvent.DigType.START_DESTROY_BLOCK;
                break;
            case ABORT_DESTROY_BLOCK:
                digType = BlockDigEvent.DigType.ABORT_DESTROY_BLOCK;
                break;
            case STOP_DESTROY_BLOCK:
                digType = BlockDigEvent.DigType.STOP_DESTROY_BLOCK;
                break;
            case DROP_ALL_ITEMS:
                interactType = ItemInteractEvent.InteractType.DROP_ALL_ITEMS;
                break;
            case DROP_ITEM:
                interactType = ItemInteractEvent.InteractType.DROP_ITEM;
                break;
            case RELEASE_USE_ITEM:
                interactType = ItemInteractEvent.InteractType.RELEASE_USE_ITEM;
                break;
        }

        if (interactType == null) {
            Vector3D pos = new Vector3D(nmsPos.getX(), nmsPos.getY(), nmsPos.getZ());
            BlockBase block = p.getWorld().getBlock(pos);
            if (block == null) {
                return null;
            }
            Direction dir = null;
            switch (nmsDir) {
                case UP:
                    dir = Direction.UP;
                    break;
                case DOWN:
                    dir = Direction.DOWN;
                    break;
                case NORTH:
                    dir = Direction.NORTH;
                    break;
                case SOUTH:
                    dir = Direction.SOUTH;
                    break;
                case WEST:
                    dir = Direction.WEST;
                    break;
                case EAST:
                    dir = Direction.EAST;
                    break;
            }
            // Event here
            return null;
        }
        org.bukkit.inventory.ItemStack itemStack = p.getInventory().getItemInHand();
        if (itemStack == null) {
            return null;
        }
        // Event here
        return null;
    }
}