package xyz.hstudio.horizon.compat.v1_8_R3;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.*;
import xyz.hstudio.horizon.api.events.outbound.*;
import xyz.hstudio.horizon.compat.IPacketConverter;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.enums.Hand;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            // Bad Move, will be blocked by the server.
            return null;
        }
        return new MoveEvent(player, to, onGround, updatePos, updateRot, moveType);
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
            return new BlockBreakEvent(player, b, BlockFace.valueOf(packet.b().name()), player.getHeldItem(), digType);
        }
        org.bukkit.inventory.ItemStack item = player.getHeldItem();
        if (item == null) {
            return null;
        }
        return new InteractItemEvent(player, item, interactType);
    }

    private Event convertBlockPlaceEvent(final HoriPlayer player, final PacketPlayInBlockPlace packet) {
        org.bukkit.inventory.ItemStack bukkitItemStack = player.getHeldItem();
        ItemStack itemStack = bukkitItemStack == null ? null : CraftItemStack.asNMSCopy(bukkitItemStack);
        BlockPosition bPos = packet.a();
        int x = bPos.getX();
        int y = bPos.getY();
        int z = bPos.getZ();
        Vector3D targetedPosition = new Vector3D(x, y, z);
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
        Vector3D interaction = new Vector3D(packet.d(), packet.e(), packet.f());
        Location placed = new Location(player.world, x, y, z);
        if (!targetedPosition.equals(new Vector3D(-1, -1, -1))) {
            BlockPlaceEvent.PlaceType placeType = itemStack != null && itemStack.getItem() instanceof ItemBlock ? BlockPlaceEvent.PlaceType.PLACE_BLOCK : BlockPlaceEvent.PlaceType.INTERACT_BLOCK;
            return new BlockPlaceEvent(player, placed, face, CraftItemStack.asBukkitCopy(itemStack).getType(), interaction, placeType);
        } else {
            if (bukkitItemStack == null) {
                return null;
            }
            return new InteractItemEvent(player, bukkitItemStack, InteractItemEvent.InteractType.START_USE_ITEM);
        }
    }

    private Event convertInteractEntityEvent(final HoriPlayer player, final PacketPlayInUseEntity packet) {
        Entity entity = packet.a(((CraftWorld) player.world).getHandle());
        if (entity == null) {
            return null;
        }
        Vec3D vec3D = packet.b();
        Vector3D intersection;
        if (vec3D == null) {
            intersection = null;
        } else {
            intersection = new Vector3D(vec3D.a, vec3D.b, vec3D.c);
        }
        InteractEntityEvent.InteractType action =
                packet.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK ?
                        InteractEntityEvent.InteractType.ATTACK :
                        InteractEntityEvent.InteractType.INTERACT;
        return new InteractEntityEvent(player, action, intersection, entity.getBukkitEntity(), Hand.MAIN_HAND);
    }

    private Event convertHeldItemEvent(final HoriPlayer player, final PacketPlayInHeldItemSlot packet) {
        return new HeldItemEvent(player, packet.a());
    }

    private Event convertSwingEvent(final HoriPlayer player, final PacketPlayInArmAnimation packet) {
        return new SwingEvent(player, Hand.MAIN_HAND);
    }

    private Event convertKeepAliveRespondEvent(final HoriPlayer player, final PacketPlayInKeepAlive packet) {
        return new KeepAliveRespondEvent(player, packet.a());
    }

    private Event convertTransaction(final HoriPlayer player, final PacketPlayInTransaction packet) {
        if (packet.a() != 0) {
            return null;
        }
        player.ping = System.currentTimeMillis() - player.lastRequestSent;
        return null;
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
        } else if (packet instanceof PacketPlayOutUpdateAttributes) {
            return convertAttributeEvent(player, (PacketPlayOutUpdateAttributes) packet);
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
        return new VehicleEvent(player, vehicle);
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
        return new VelocityEvent(player, x, y, z);
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
            return new MetaEvent(player, objects);
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
            int id = serializer.e();
            if (id != player.player.getEntityId()) {
                return null;
            }
            List<AttributeEvent.AttributeSnapshot> snapshots = new ArrayList<>();
            int size = serializer.readInt();
            for (int var1 = 0; var1 < size; ++var1) {
                String key = serializer.c(64);
                double baseValue = serializer.readDouble();
                int magic = serializer.e();
                List<AttributeEvent.AttributeModifier> modifiers = new ArrayList<>();
                for (int var2 = 0; var2 < magic; ++var2) {
                    UUID uuid = serializer.g();
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
}