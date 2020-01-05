package xyz.hstudio.horizon.bukkit.compat.v1_15_R1;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.PacketConvert;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.network.events.inbound.*;
import xyz.hstudio.horizon.bukkit.network.events.outbound.VehicleEvent;
import xyz.hstudio.horizon.bukkit.util.Hand;
import xyz.hstudio.horizon.bukkit.util.Location;
import xyz.hstudio.horizon.bukkit.util.MathUtils;

import java.util.stream.IntStream;

public class PacketConvert_v1_15_R1 extends PacketConvert {

    @Override
    public Event convertIn(final HoriPlayer player, final Object packet) {
        if (packet instanceof PacketPlayInEntityAction) {
            return convertActionEvent(player, (PacketPlayInEntityAction) packet);
        } else if (packet instanceof PacketPlayInFlying) {
            return convertMoveEvent(player, (PacketPlayInFlying) packet);
        } else if (packet instanceof PacketPlayInBlockDig) {
            return convertBlockBreakEvent(player, (PacketPlayInBlockDig) packet);
        } else if (packet instanceof PacketPlayInUseItem) {
            return convertBlockPlaceEvent(player, (PacketPlayInUseItem) packet);
        } else if (packet instanceof PacketPlayInUseEntity) {
            return convertInteractEntityEvent(player, (PacketPlayInUseEntity) packet);
        } else if (packet instanceof PacketPlayInHeldItemSlot) {
            return convertHeldItemEvent(player, (PacketPlayInHeldItemSlot) packet);
        } else if (packet instanceof PacketPlayInArmAnimation) {
            return convertSwingEvent(player, (PacketPlayInArmAnimation) packet);
        }
        return null;
    }

    private Event convertActionEvent(final HoriPlayer player, final PacketPlayInEntityAction packet) {
        ActionEvent.Action action;
        switch (packet.c()) {
            case PRESS_SHIFT_KEY:
                action = ActionEvent.Action.START_SNEAKING;
                break;
            case RELEASE_SHIFT_KEY:
                action = ActionEvent.Action.STOP_SNEAKING;
                break;
            case START_SPRINTING:
                action = ActionEvent.Action.START_SPRINTING;
                break;
            case STOP_SPRINTING:
                action = ActionEvent.Action.STOP_SPRINTING;
                break;
            case START_FALL_FLYING:
                action = ActionEvent.Action.START_GLIDING;
                break;
            default:
                return null;
        }
        return new ActionEvent(player, action, new WrappedPacket(packet));
    }

    private Event convertMoveEvent(final HoriPlayer player, final PacketPlayInFlying packet) {
        boolean updatePos = false;
        boolean updateRot = false;
        MoveEvent.MoveType moveType = MoveEvent.MoveType.FLYING;
        if (packet instanceof PacketPlayInFlying.PacketPlayInPosition) {
            updatePos = true;
            moveType = MoveEvent.MoveType.POSITION;
        } else if (packet instanceof PacketPlayInFlying.PacketPlayInLook) {
            updateRot = true;
            moveType = MoveEvent.MoveType.LOOK;
        } else if (packet instanceof PacketPlayInFlying.PacketPlayInPositionLook) {
            updatePos = true;
            updateRot = true;
            moveType = MoveEvent.MoveType.POSITION_LOOK;
        }
        double x = packet.a(player.position.x);
        double y = packet.b(player.position.y);
        double z = packet.c(player.position.z);
        float yaw = packet.a(player.position.yaw);
        float pitch = packet.b(player.position.pitch);
        boolean onGround = packet.b();
        Location to = new Location(player.player.getWorld(), x, y, z, yaw, pitch);
        if (MathUtils.abs(to.x) >= Integer.MAX_VALUE || MathUtils.abs(to.y) >= Integer.MAX_VALUE || MathUtils.abs(to.z) >= Integer.MAX_VALUE ||
                Double.isNaN(to.x) || Double.isNaN(to.y) || Double.isNaN(to.z)) {
            return new BadMoveEvent(player, new WrappedPacket(packet));
        }
        return new MoveEvent(player, to, onGround, updatePos, updateRot, moveType, new WrappedPacket(packet));
    }

    private Event convertBlockBreakEvent(final HoriPlayer player, final PacketPlayInBlockDig packet) {
        BlockBreakEvent.DigType digType = null;
        InteractItemEvent.InteractType interactType = null;
        switch (packet.d()) {
            case START_DESTROY_BLOCK:
                digType = BlockBreakEvent.DigType.START;
                break;
            case ABORT_DESTROY_BLOCK:
                digType = BlockBreakEvent.DigType.CANCEL;
                break;
            case STOP_DESTROY_BLOCK:
                digType = BlockBreakEvent.DigType.COMPLETE;
                break;
            case DROP_ALL_ITEMS:
                interactType = InteractItemEvent.InteractType.DROP_HELD_ITEM_STACK;
                break;
            case DROP_ITEM:
                interactType = InteractItemEvent.InteractType.DROP_HELD_ITEM;
                break;
            case RELEASE_USE_ITEM:
                interactType = InteractItemEvent.InteractType.RELEASE_USE_ITEM;
                break;
            default:
                return null;
        }
        if (interactType == null) {
            BlockPosition pos = packet.b();
            Location loc = new Location(player.world, pos.getX(), pos.getY(), pos.getZ());
            org.bukkit.block.Block b = loc.getBlock();
            if (b == null) {
                return null;
            }
            return new BlockBreakEvent(player, b, BlockFace.valueOf(packet.c().name()), player.getHeldItem(), digType, new WrappedPacket(packet));
        }
        // TODO: Check for main/off hand?
        org.bukkit.inventory.ItemStack item = player.getHeldItem();
        if (item == null) {
            return null;
        }
        return new InteractItemEvent(player, item, interactType, new WrappedPacket(packet));
    }

    private Event convertBlockPlaceEvent(final HoriPlayer player, final PacketPlayInUseItem packet) {
        ItemStack itemStack = ((CraftPlayer) player.player).getHandle().b(packet.b());
        BlockPosition bPos = packet.c().getBlockPosition();
        int x = bPos.getX();
        int y = bPos.getY();
        int z = bPos.getZ();
        Vector targetedPosition = new Vector(x, y, z);
        BlockPlaceEvent.BlockFace face;
        switch (packet.c().getDirection()) {
            case DOWN:
                y -= 1;
                face = BlockPlaceEvent.BlockFace.BOTTOM;
                break;
            case UP:
                y += 1;
                face = BlockPlaceEvent.BlockFace.TOP;
                break;
            case NORTH:
                z -= 1;
                face = BlockPlaceEvent.BlockFace.NORTH;
                break;
            case SOUTH:
                z += 1;
                face = BlockPlaceEvent.BlockFace.SOUTH;
                break;
            case WEST:
                x -= 1;
                face = BlockPlaceEvent.BlockFace.WEST;
                break;
            case EAST:
                x += 1;
                face = BlockPlaceEvent.BlockFace.EAST;
                break;
            default:
                face = BlockPlaceEvent.BlockFace.INVALID;
                break;
        }
        Vec3D pos = packet.c().getPos();
        Vector interaction = new Vector(pos.x, pos.y, pos.z).subtract(targetedPosition);
        Location placed = new Location(player.world, x, y, z);
        if (!targetedPosition.equals(new Vector(-1, -1, -1))) {
            BlockPlaceEvent.PlaceType placeType = itemStack != null && itemStack.getItem() instanceof ItemBlock ? BlockPlaceEvent.PlaceType.PLACE_BLOCK : BlockPlaceEvent.PlaceType.INTERACT_BLOCK;
            // Does CraftItemStack#asBukkitCopy perform well?
            return new BlockPlaceEvent(player, placed, face, itemStack == null ? org.bukkit.Material.AIR : CraftItemStack.asBukkitCopy(itemStack).getType(), interaction, placeType, new WrappedPacket(packet));
        }
        return null;
    }

    private Event convertInteractEntityEvent(final HoriPlayer player, final PacketPlayInUseEntity packet) {
        Entity entity = packet.a(((CraftWorld) player.world).getHandle());
        if (entity == null) {
            return null;
        }
        InteractEntityEvent.InteractType action =
                packet.b() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK ?
                        InteractEntityEvent.InteractType.ATTACK :
                        InteractEntityEvent.InteractType.INTERACT;
        return new InteractEntityEvent(player, action, entity.getBukkitEntity(), Hand.MAIN_HAND, new WrappedPacket(packet));
    }

    private Event convertHeldItemEvent(final HoriPlayer player, final PacketPlayInHeldItemSlot packet) {
        return new HeldItemEvent(player, packet.b(), new WrappedPacket(packet));
    }

    private Event convertSwingEvent(final HoriPlayer player, final PacketPlayInArmAnimation packet) {
        return new SwingEvent(player, Hand.valueOf(packet.b().name()), new WrappedPacket(packet));
    }

    @Override
    public Event convertOut(final HoriPlayer player, final Object packet) {
        if (packet instanceof PacketPlayOutMount) {
            return convertAttachEvent(player, (PacketPlayOutMount) packet);
        }
        return null;
    }

    private Event convertAttachEvent(final HoriPlayer player, final PacketPlayOutMount packet) {
        // Faster than reflection?
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(16));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            return null;
        }
        int vehicle = serializer.i();
        int[] passengers = serializer.b();
        // Is it efficient?
        if (IntStream.of(passengers).anyMatch(i -> i == player.player.getEntityId())) {
            return new VehicleEvent(player, vehicle, new WrappedPacket(packet));
        } else if (player.vehicle == vehicle) {
            return new VehicleEvent(player, -1, new WrappedPacket(packet));
        }
        return null;
    }
}