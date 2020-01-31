package xyz.hstudio.horizon.bukkit.compat.v1_8_R3;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import xyz.hstudio.horizon.bukkit.compat.IPacketConverter;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.network.events.inbound.*;
import xyz.hstudio.horizon.bukkit.network.events.outbound.*;
import xyz.hstudio.horizon.bukkit.util.Hand;
import xyz.hstudio.horizon.bukkit.util.Location;
import xyz.hstudio.horizon.bukkit.util.MathUtils;
import xyz.hstudio.horizon.bukkit.util.Vec3D;

import java.util.ArrayList;
import java.util.List;

public class PacketConverter_v1_8_R3 implements IPacketConverter {

    @Override
    public Event convertIn(final HoriPlayer player, final Object packet) {
        if (packet instanceof PacketPlayInEntityAction) {
            return convertActionEvent(player, (PacketPlayInEntityAction) packet);
        } else if (packet instanceof PacketPlayInFlying) {
            return convertMoveEvent(player, (PacketPlayInFlying) packet);
        } else if (packet instanceof PacketPlayInBlockDig) {
            return convertBlockBreakEvent(player, (PacketPlayInBlockDig) packet);
        } else if (packet instanceof PacketPlayInBlockPlace) {
            return convertBlockPlaceEvent(player, (PacketPlayInBlockPlace) packet);
        } else if (packet instanceof PacketPlayInUseEntity) {
            return convertInteractEntityEvent(player, (PacketPlayInUseEntity) packet);
        } else if (packet instanceof PacketPlayInHeldItemSlot) {
            return convertHeldItemEvent(player, (PacketPlayInHeldItemSlot) packet);
        } else if (packet instanceof PacketPlayInArmAnimation) {
            return convertSwingEvent(player, (PacketPlayInArmAnimation) packet);
        } else if (packet instanceof PacketPlayInKeepAlive) {
            return convertKeepAliveRespondEvent(player, (PacketPlayInKeepAlive) packet);
        } else if (packet instanceof PacketPlayInTransaction) {
            return convertTransaction(player, (PacketPlayInTransaction) packet);
        } else if (packet instanceof PacketPlayInWindowClick) {
            return convertWindowClickEvent(player, (PacketPlayInWindowClick) packet);
        } else if (packet instanceof PacketPlayInCloseWindow) {
            return convertWindowCloseEvent(player, (PacketPlayInCloseWindow) packet);
        } else if (packet instanceof PacketPlayInClientCommand) {
            return convertClientCommandEvent(player, (PacketPlayInClientCommand) packet);
        } else if (packet instanceof PacketPlayInAbilities) {
            return convertAbilitiesEvent(player, (PacketPlayInAbilities) packet);
        }
        return null;
    }

    private Event convertActionEvent(final HoriPlayer player, final PacketPlayInEntityAction packet) {
        ActionEvent.Action action;
        switch (packet.b()) {
            case START_SNEAKING:
                action = ActionEvent.Action.START_SNEAKING;
                break;
            case STOP_SNEAKING:
                action = ActionEvent.Action.STOP_SNEAKING;
                break;
            case START_SPRINTING:
                action = ActionEvent.Action.START_SPRINTING;
                break;
            case STOP_SPRINTING:
                action = ActionEvent.Action.STOP_SPRINTING;
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
        boolean hasPos = packet.g();
        boolean hasLook = packet.h();
        double x = hasPos ? packet.a() : player.position.x;
        double y = hasPos ? packet.b() : player.position.y;
        double z = hasPos ? packet.c() : player.position.z;
        float yaw = hasLook ? packet.d() : player.position.yaw;
        float pitch = hasLook ? packet.e() : player.position.pitch;
        boolean onGround = packet.f();
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
        switch (packet.c()) {
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
            BlockPosition pos = packet.a();
            Location loc = new Location(player.world, pos.getX(), pos.getY(), pos.getZ());
            org.bukkit.block.Block b = loc.getBlock();
            if (b == null) {
                return null;
            }
            return new BlockBreakEvent(player, b, BlockFace.valueOf(packet.b().name()), player.getHeldItem(), digType, new WrappedPacket(packet));
        }
        org.bukkit.inventory.ItemStack item = player.getHeldItem();
        if (item == null) {
            return null;
        }
        return new InteractItemEvent(player, item, interactType, new WrappedPacket(packet));
    }

    private Event convertBlockPlaceEvent(final HoriPlayer player, final PacketPlayInBlockPlace packet) {
        org.bukkit.inventory.ItemStack bukkitItemStack = player.getHeldItem();
        ItemStack itemStack = bukkitItemStack == null ? null : CraftItemStack.asNMSCopy(bukkitItemStack);
        BlockPosition bPos = packet.a();
        int x = bPos.getX();
        int y = bPos.getY();
        int z = bPos.getZ();
        Vec3D targetedPosition = new Vec3D(x, y, z);
        BlockPlaceEvent.BlockFace face;
        switch (packet.getFace()) {
            case 0:
                face = BlockPlaceEvent.BlockFace.BOTTOM;
                y -= 1;
                break;
            case 1:
                face = BlockPlaceEvent.BlockFace.TOP;
                y += 1;
                break;
            case 2:
                face = BlockPlaceEvent.BlockFace.NORTH;
                z -= 1;
                break;
            case 3:
                face = BlockPlaceEvent.BlockFace.SOUTH;
                z += 1;
                break;
            case 4:
                face = BlockPlaceEvent.BlockFace.WEST;
                x -= 1;
                break;
            case 5:
                face = BlockPlaceEvent.BlockFace.EAST;
                x += 1;
                break;
            default:
                face = BlockPlaceEvent.BlockFace.INVALID;
                break;
        }
        Vec3D interaction = new Vec3D(packet.d(), packet.e(), packet.f());
        Location placed = new Location(player.world, x, y, z);
        if (!targetedPosition.equals(new Vec3D(-1, -1, -1))) {
            BlockPlaceEvent.PlaceType placeType = itemStack != null && itemStack.getItem() instanceof ItemBlock ? BlockPlaceEvent.PlaceType.PLACE_BLOCK : BlockPlaceEvent.PlaceType.INTERACT_BLOCK;
            return new BlockPlaceEvent(player, placed, face, CraftItemStack.asBukkitCopy(itemStack).getType(), interaction, placeType, new WrappedPacket(packet));
        } else {
            if (bukkitItemStack == null) {
                return null;
            }
            return new InteractItemEvent(player, bukkitItemStack, InteractItemEvent.InteractType.START_USE_ITEM, new WrappedPacket(packet));
        }
    }

    private Event convertInteractEntityEvent(final HoriPlayer player, final PacketPlayInUseEntity packet) {
        Entity entity = packet.a(((CraftWorld) player.world).getHandle());
        if (entity == null) {
            return null;
        }
        InteractEntityEvent.InteractType action =
                packet.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK ?
                        InteractEntityEvent.InteractType.ATTACK :
                        InteractEntityEvent.InteractType.INTERACT;
        return new InteractEntityEvent(player, action, entity.getBukkitEntity(), Hand.MAIN_HAND, new WrappedPacket(packet));
    }

    private Event convertHeldItemEvent(final HoriPlayer player, final PacketPlayInHeldItemSlot packet) {
        return new HeldItemEvent(player, packet.a(), new WrappedPacket(packet));
    }

    private Event convertSwingEvent(final HoriPlayer player, final PacketPlayInArmAnimation packet) {
        return new SwingEvent(player, Hand.MAIN_HAND, new WrappedPacket(packet));
    }

    private Event convertKeepAliveRespondEvent(final HoriPlayer player, final PacketPlayInKeepAlive packet) {
        return new KeepAliveRespondEvent(player, packet.a(), new WrappedPacket(packet));
    }

    private Event convertTransaction(final HoriPlayer player, final PacketPlayInTransaction packet) {
        if (packet.a() != 0) {
            return null;
        }
        player.ping = System.currentTimeMillis() - player.lastRequestSent;
        return null;
    }

    private Event convertWindowClickEvent(final HoriPlayer player, final PacketPlayInWindowClick packet) {
        return new WindowClickEvent(player, packet.a(), packet.b(), packet.c(), new WrappedPacket(packet));
    }

    private Event convertWindowCloseEvent(final HoriPlayer player, final PacketPlayInCloseWindow packet) {
        return new WindowCloseEvent(player, new WrappedPacket(packet));
    }

    private Event convertClientCommandEvent(final HoriPlayer player, final PacketPlayInClientCommand packet) {
        return new ClientCommandEvent(player, ClientCommandEvent.ClientCommand.valueOf(packet.a().name()), new WrappedPacket(packet));
    }

    private Event convertAbilitiesEvent(final HoriPlayer player, final PacketPlayInAbilities packet) {
        return new AbilitiesEvent(player, packet.isFlying(), new WrappedPacket(packet));
    }

    @Override
    public Event convertOut(final HoriPlayer player, final Object packet) {
        if (packet instanceof PacketPlayOutAttachEntity) {
            return convertAttachEvent(player, (PacketPlayOutAttachEntity) packet);
        } else if (packet instanceof PacketPlayOutEntityVelocity) {
            return convertVelocityEvent(player, (PacketPlayOutEntityVelocity) packet);
        } else if (packet instanceof PacketPlayOutEntityMetadata) {
            return convertMetaEvent(player, (PacketPlayOutEntityMetadata) packet);
        } else if (packet instanceof PacketPlayOutOpenWindow) {
            return convertOpenWindowEvent(player, (PacketPlayOutOpenWindow) packet);
        } else if (packet instanceof PacketPlayOutCloseWindow) {
            return convertCloseWindowEvent(player, (PacketPlayOutCloseWindow) packet);
        }
        return null;
    }

    private Event convertAttachEvent(final HoriPlayer player, final PacketPlayOutAttachEntity packet) {
        // Faster than reflection?
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(16));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            return null;
        }
        int id = serializer.readInt();
        // -1 = dismount/unleash
        int vehicle = serializer.readInt();
        // 0 = mount/dismount, 1 = leash/unleash
        int action = serializer.readUnsignedByte();
        if (action != 0 || id != player.player.getEntityId()) {
            return null;
        }
        return new VehicleEvent(player, vehicle, new WrappedPacket(packet));
    }

    private Event convertVelocityEvent(final HoriPlayer player, final PacketPlayOutEntityVelocity packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(8));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            return null;
        }
        int id = serializer.e();
        if (id != player.player.getEntityId()) {
            return null;
        }
        double x = serializer.readShort() / 8000D;
        double y = serializer.readShort() / 8000D;
        double z = serializer.readShort() / 8000D;
        return new VelocityEvent(player, x, y, z, new WrappedPacket(packet));
    }

    private Event convertMetaEvent(final HoriPlayer player, final PacketPlayOutEntityMetadata packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(0));
        try {
            packet.b(serializer);
            int id = serializer.e();
            if (id != player.player.getEntityId()) {
                return null;
            }
            List<DataWatcher.WatchableObject> metaData = DataWatcher.b(serializer);
            if (metaData == null) {
                return null;
            }
            List<MetaEvent.WatchableObject> objects = new ArrayList<>();
            for (DataWatcher.WatchableObject watchableObject : metaData) {
                int index = watchableObject.a();
                Object object = watchableObject.b();
                objects.add(new MetaEvent.WatchableObject(index, object));
            }
            return new MetaEvent(player, objects, new WrappedPacket(packet));
        } catch (Exception e) {
            return null;
        }
    }

    private Event convertOpenWindowEvent(final HoriPlayer player, final PacketPlayOutOpenWindow packet) {
        return new OpenWindowEvent(player, new WrappedPacket(packet));
    }

    private Event convertCloseWindowEvent(final HoriPlayer player, final PacketPlayOutCloseWindow packet) {
        return new CloseWindowEvent(player, new WrappedPacket(packet));
    }
}