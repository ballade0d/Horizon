package xyz.hstudio.horizon.compat.v1_12_R1;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_12_R1.*;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import xyz.hstudio.horizon.compat.IPacketConverter;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.events.inbound.*;
import xyz.hstudio.horizon.events.outbound.*;
import xyz.hstudio.horizon.util.enums.BlockFace;
import xyz.hstudio.horizon.util.enums.Hand;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class PacketConverter_v1_12_R1 implements IPacketConverter {

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
        } else if (packet instanceof PacketPlayInBlockPlace) {
            return convertInteractItemEvent(player, (PacketPlayInBlockPlace) packet);
        } else if (packet instanceof PacketPlayInUseEntity) {
            return convertInteractEntityEvent(player, (PacketPlayInUseEntity) packet);
        } else if (packet instanceof PacketPlayInHeldItemSlot) {
            return convertHeldItemEvent(player, (PacketPlayInHeldItemSlot) packet);
        } else if (packet instanceof PacketPlayInArmAnimation) {
            return convertSwingEvent(player, (PacketPlayInArmAnimation) packet);
        } else if (packet instanceof PacketPlayInKeepAlive) {
            return convertKeepAliveRespondEvent(player, (PacketPlayInKeepAlive) packet);
        } else if (packet instanceof PacketPlayInWindowClick) {
            return convertWindowClickEvent(player, (PacketPlayInWindowClick) packet);
        } else if (packet instanceof PacketPlayInCloseWindow) {
            return convertWindowCloseEvent(player, (PacketPlayInCloseWindow) packet);
        } else if (packet instanceof PacketPlayInClientCommand) {
            return convertClientCommandEvent(player, (PacketPlayInClientCommand) packet);
        } else if (packet instanceof PacketPlayInAbilities) {
            return convertAbilitiesEvent(player, (PacketPlayInAbilities) packet);
        } else if (packet instanceof PacketPlayInCustomPayload) {
            return convertCustomPayloadEvent(player, (PacketPlayInCustomPayload) packet);
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
            case START_FALL_FLYING:
                action = ActionEvent.Action.START_GLIDING;
                break;
            default:
                return null;
        }
        return new ActionEvent(player, action);
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
        boolean onGround = packet.a();
        Location to = new Location(player.getPlayer().getWorld(), x, y, z, yaw, pitch);
        if (Math.abs(to.x) >= Integer.MAX_VALUE || Math.abs(to.y) >= Integer.MAX_VALUE || Math.abs(to.z) >= Integer.MAX_VALUE ||
                Double.isNaN(to.x) || Double.isNaN(to.y) || Double.isNaN(to.z)) {
            // Bad Move, will be blocked by the server.
            return null;
        }
        return new MoveEvent(player, to, onGround, updatePos, updateRot, moveType, packet);
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
            IWrappedBlock b = loc.getBlock();
            if (b == null) {
                return null;
            }
            BlockFace face;
            switch (packet.b()) {
                case UP:
                    face = BlockFace.TOP;
                    break;
                case DOWN:
                    face = BlockFace.BOTTOM;
                    break;
                case NORTH:
                    face = BlockFace.NORTH;
                    break;
                case SOUTH:
                    face = BlockFace.SOUTH;
                    break;
                case WEST:
                    face = BlockFace.WEST;
                    break;
                case EAST:
                    face = BlockFace.EAST;
                    break;
                default:
                    return null;
            }
            return new BlockBreakEvent(player, b, face, player.getHeldItem(), digType);
        }
        org.bukkit.inventory.ItemStack item = player.getHeldItem();
        if (item == null) {
            return null;
        }
        return new InteractItemEvent(player, item, InteractItemEvent.Hand.MAIN, interactType);
    }

    private Event convertBlockPlaceEvent(final HoriPlayer player, final PacketPlayInUseItem packet) {
        org.bukkit.inventory.ItemStack bkItem = packet.c() == EnumHand.MAIN_HAND ? player.getHeldItem() :
                ((CraftInventoryPlayer) player.getPlayer().getInventory()).getItemInOffHand();
        ItemStack itemStack = CraftItemStack.asNMSCopy(bkItem);
        BlockPosition bPos = packet.a();
        int x = bPos.getX();
        int y = bPos.getY();
        int z = bPos.getZ();
        Vector3D targetedPosition = new Vector3D(x, y, z);
        BlockFace face;
        switch (packet.b()) {
            case DOWN:
                y -= 1;
                face = BlockFace.BOTTOM;
                break;
            case UP:
                y += 1;
                face = BlockFace.TOP;
                break;
            case NORTH:
                z -= 1;
                face = BlockFace.NORTH;
                break;
            case SOUTH:
                z += 1;
                face = BlockFace.SOUTH;
                break;
            case WEST:
                x -= 1;
                face = BlockFace.WEST;
                break;
            case EAST:
                x += 1;
                face = BlockFace.EAST;
                break;
            default:
                face = BlockFace.INVALID;
                break;
        }
        Vector3D interaction = new Vector3D(packet.d(), packet.e(), packet.f());
        Location placed = new Location(player.world, x, y, z);
        if (!targetedPosition.equals(new Vector3D(-1, -1, -1))) {
            BlockPlaceEvent.PlaceType placeType = itemStack != null && itemStack.getItem() instanceof ItemBlock ? BlockPlaceEvent.PlaceType.PLACE_BLOCK : BlockPlaceEvent.PlaceType.INTERACT_BLOCK;
            return new BlockPlaceEvent(player, placed, face, bkItem == null ? org.bukkit.Material.AIR : bkItem.getType(), interaction, placeType);
        }
        return null;
    }

    private Event convertInteractItemEvent(final HoriPlayer player, final PacketPlayInBlockPlace packet) {
        boolean main = packet.a() == EnumHand.MAIN_HAND;
        org.bukkit.inventory.ItemStack bukkitItemStack =
                main ? player.getHeldItem() : ((CraftInventoryPlayer) player.getPlayer().getInventory()).getItemInOffHand();
        return new InteractItemEvent(player, bukkitItemStack, main ? InteractItemEvent.Hand.MAIN : InteractItemEvent.Hand.OFF, InteractItemEvent.InteractType.START_USE_ITEM);
    }

    private Event convertInteractEntityEvent(final HoriPlayer player, final PacketPlayInUseEntity packet) {
        Entity entity = packet.a(((CraftWorld) player.world).getHandle());
        Vec3D vec3D = packet.c();
        Vector3D intersection;
        if (vec3D == null) {
            intersection = null;
        } else {
            intersection = new Vector3D(vec3D.x, vec3D.y, vec3D.z);
        }
        InteractEntityEvent.InteractType action =
                packet.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK ?
                        InteractEntityEvent.InteractType.ATTACK :
                        InteractEntityEvent.InteractType.INTERACT;
        if (entity == null) {
            PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
            try {
                packet.b(serializer);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new InteractCSEntityEvent(player, action, serializer.g());
        }
        return new InteractEntityEvent(player, action, intersection, entity.getBukkitEntity(), Hand.MAIN_HAND);
    }

    private Event convertHeldItemEvent(final HoriPlayer player, final PacketPlayInHeldItemSlot packet) {
        return new HeldItemEvent(player, packet.a());
    }

    private Event convertSwingEvent(final HoriPlayer player, final PacketPlayInArmAnimation packet) {
        return new SwingEvent(player, Hand.valueOf(packet.a().name()));
    }

    private Event convertKeepAliveRespondEvent(final HoriPlayer player, final PacketPlayInKeepAlive packet) {
        return new KeepAliveRespondEvent(player, packet.a());
    }

    private Event convertWindowClickEvent(final HoriPlayer player, final PacketPlayInWindowClick packet) {
        return new WindowClickEvent(player, packet.a(), packet.b(), packet.c());
    }

    private Event convertWindowCloseEvent(final HoriPlayer player, final PacketPlayInCloseWindow packet) {
        return new WindowCloseEvent(player);
    }

    private Event convertClientCommandEvent(final HoriPlayer player, final PacketPlayInClientCommand packet) {
        return new ClientCommandEvent(player, ClientCommandEvent.ClientCommand.valueOf(packet.a().name()));
    }

    private Event convertAbilitiesEvent(final HoriPlayer player, final PacketPlayInAbilities packet) {
        return new AbilitiesEvent(player, packet.isFlying());
    }

    private Event convertCustomPayloadEvent(final HoriPlayer player, final PacketPlayInCustomPayload packet) {
        return new CustomPayloadEvent(player, packet.a(), packet.b().readableBytes());
    }

    @Override
    public Event convertOut(final HoriPlayer player, final Object packet) {
        if (packet instanceof PacketPlayOutMount) {
            return convertAttachEvent(player, (PacketPlayOutMount) packet);
        } else if (packet instanceof PacketPlayOutEntityVelocity) {
            return convertVelocityEvent(player, (PacketPlayOutEntityVelocity) packet);
        } else if (packet instanceof PacketPlayOutExplosion) {
            return convertVelocityEvent(player, (PacketPlayOutExplosion) packet);
        } else if (packet instanceof PacketPlayOutEntityMetadata) {
            return convertMetaEvent(player, (PacketPlayOutEntityMetadata) packet);
        } else if (packet instanceof PacketPlayOutOpenWindow) {
            return convertOpenWindowEvent(player, (PacketPlayOutOpenWindow) packet);
        } else if (packet instanceof PacketPlayOutCloseWindow) {
            return convertCloseWindowEvent(player, (PacketPlayOutCloseWindow) packet);
        } else if (packet instanceof PacketPlayOutUpdateAttributes) {
            return convertAttributeEvent(player, (PacketPlayOutUpdateAttributes) packet);
        } else if (packet instanceof PacketPlayOutEntity) {
            return convertUpdatePosEvent(player, (PacketPlayOutEntity) packet);
        } else if (packet instanceof PacketPlayOutUpdateHealth) {
            return convertUpdateHealthEvent(player, (PacketPlayOutUpdateHealth) packet);
        } else if (packet instanceof PacketPlayOutEntityDestroy) {
            return convertEntityDestroyEvent(player, (PacketPlayOutEntityDestroy) packet);
        } else if (packet instanceof PacketPlayOutBlockChange) {
            return convertBlockUpdateEvent(player, (PacketPlayOutBlockChange) packet);
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
        int vehicle = serializer.g();
        int[] passengers = serializer.b();
        // Is it efficient?
        if (IntStream.of(passengers).anyMatch(i -> i == player.getPlayer().getEntityId())) {
            return new VehicleEvent(player, vehicle);
        } else if (player.vehicle == vehicle) {
            return new VehicleEvent(player, -1);
        }
        return null;
    }

    private Event convertVelocityEvent(final HoriPlayer player, final PacketPlayOutEntityVelocity packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(8));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            return null;
        }
        int id = serializer.g();
        if (id != player.getPlayer().getEntityId()) {
            return null;
        }
        double x = serializer.readShort() / 8000D;
        double y = serializer.readShort() / 8000D;
        double z = serializer.readShort() / 8000D;
        return new VelocityEvent(player, x, y, z);
    }

    private Event convertVelocityEvent(final HoriPlayer player, final PacketPlayOutExplosion packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(8));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            return null;
        }
        serializer.readerIndex(serializer.writerIndex() - 12);
        float x = serializer.readFloat();
        float y = serializer.readFloat();
        float z = serializer.readFloat();
        return new VelocityEvent(player, x, y, z);
    }

    private Event convertMetaEvent(final HoriPlayer player, final PacketPlayOutEntityMetadata packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(0));
        try {
            packet.b(serializer);
            int id = serializer.g();
            List<DataWatcher.Item<?>> metaData = DataWatcher.b(serializer);
            if (metaData == null) {
                return null;
            }
            List<MetaEvent.WatchableObject> objects = new ArrayList<>();
            for (DataWatcher.Item<?> watchableObject : metaData) {
                int index = watchableObject.a().a();
                Object object = watchableObject.b();
                objects.add(new MetaEvent.WatchableObject(index, object));
            }
            return new MetaEvent(player, id, objects, packet);
        } catch (Exception e) {
            return null;
        }
    }

    private Event convertOpenWindowEvent(final HoriPlayer player, final PacketPlayOutOpenWindow packet) {
        return new OpenWindowEvent(player);
    }

    private Event convertCloseWindowEvent(final HoriPlayer player, final PacketPlayOutCloseWindow packet) {
        return new CloseWindowEvent(player);
    }

    private Event convertAttributeEvent(final HoriPlayer player, final PacketPlayOutUpdateAttributes packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(0));
        try {
            packet.b(serializer);
            int id = serializer.g();
            if (id != player.getPlayer().getEntityId()) {
                return null;
            }
            List<AttributeEvent.AttributeSnapshot> snapshots = new ArrayList<>();
            int size = serializer.readInt();
            for (int var1 = 0; var1 < size; ++var1) {
                String key = serializer.e(64);
                double baseValue = serializer.readDouble();
                int magic = serializer.g();
                List<AttributeEvent.AttributeModifier> modifiers = new ArrayList<>();
                for (int var2 = 0; var2 < magic; ++var2) {
                    UUID uuid = serializer.i();
                    double value = serializer.readDouble();
                    byte operation = serializer.readByte();
                    modifiers.add(new AttributeEvent.AttributeModifier(uuid, value, operation));
                }
                snapshots.add(new AttributeEvent.AttributeSnapshot(key, baseValue, magic, modifiers));
            }
            return new AttributeEvent(player, snapshots);
        } catch (Exception e) {
            return null;
        }
    }

    private Event convertUpdatePosEvent(final HoriPlayer player, final PacketPlayOutEntity packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(16));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int id = serializer.g();
        Entity nmsEntity = ((CraftWorld) player.position.world).getHandle().getEntity(id);
        if (!(nmsEntity instanceof EntityPlayer)) {
            return null;
        }
        return new UpdatePosEvent(player, packet);
    }

    private Event convertUpdateHealthEvent(final HoriPlayer player, final PacketPlayOutUpdateHealth packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(16));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UpdateHealthEvent(player, serializer.readFloat(), serializer.g(), serializer.readFloat());
    }

    private Event convertEntityDestroyEvent(final HoriPlayer player, final PacketPlayOutEntityDestroy packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(8));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[] ids = new int[serializer.g()];
        for (int i = 0; i < ids.length; ++i) {
            ids[i] = serializer.g();
        }
        if (ArrayUtils.contains(ids, player.vehicle)) {
            player.vehicle = -1;
            player.leaveVehicleTick = player.currentTick;
            player.sendSimulatedAction(() -> player.leaveVehicleTick = player.currentTick);
        }
        return null;
    }

    private Event convertBlockUpdateEvent(final HoriPlayer player, final PacketPlayOutBlockChange packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(8));
        try {
            packet.b(serializer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BlockPosition pos = serializer.e();
        IBlockData data = Block.REGISTRY_ID.fromId(serializer.g());
        return new BlockUpdateEvent(player, IWrappedBlock.create(player.world, pos, data));
    }
}