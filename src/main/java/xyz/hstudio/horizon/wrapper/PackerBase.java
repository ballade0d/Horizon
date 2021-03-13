package xyz.hstudio.horizon.wrapper;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.*;
import xyz.hstudio.horizon.event.outbound.*;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PackerBase {

    private static final Vector3D INVALID_7 = new Vector3D(-1, 255, -1);
    private static final Vector3D INVALID_8 = new Vector3D(-1, -1, -1);

    public static Event<?> received(HPlayer p, Object packet) {
        if (packet instanceof PacketPlayInAbilities) {
            return toEvent(p, (PacketPlayInAbilities) packet);
        } else if (packet instanceof PacketPlayInArmAnimation) {
            return toEvent(p, (PacketPlayInArmAnimation) packet);
        } else if (packet instanceof PacketPlayInBlockDig) {
            return toEvent(p, (PacketPlayInBlockDig) packet);
        } else if (packet instanceof PacketPlayInBlockPlace) {
            return toEvent(p, (PacketPlayInBlockPlace) packet);
        } else if (packet instanceof PacketPlayInCloseWindow) {
            return toEvent(p, (PacketPlayInCloseWindow) packet);
        } else if (packet instanceof PacketPlayInEntityAction) {
            return toEvent(p, (PacketPlayInEntityAction) packet);
        } else if (packet instanceof PacketPlayInFlying.PacketPlayInLook) {
            return toEvent(p, (PacketPlayInFlying.PacketPlayInLook) packet);
        } else if (packet instanceof PacketPlayInFlying.PacketPlayInPosition) {
            return toEvent(p, (PacketPlayInFlying.PacketPlayInPosition) packet);
        } else if (packet instanceof PacketPlayInFlying.PacketPlayInPositionLook) {
            return toEvent(p, (PacketPlayInFlying.PacketPlayInPositionLook) packet);
        } else if (packet instanceof PacketPlayInHeldItemSlot) {
            return toEvent(p, (PacketPlayInHeldItemSlot) packet);
        } else if (packet instanceof PacketPlayInUseEntity) {
            return toEvent(p, (PacketPlayInUseEntity) packet);
        } else if (packet instanceof PacketPlayInKeepAlive) {
            return toEvent(p, (PacketPlayInKeepAlive) packet);
        }
        return null;
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInAbilities packet) {
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

        return new CAbilitiesEvent(p, invulnerable, flying, canFly, inCreative, flyingSpeed, walkingSpeed);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInArmAnimation packet) {
        return new ArmSwingEvent(p);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInBlockDig packet) {
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
            default:
                return null;
        }

        if (interactType == null) {
            Vector3D pos = new Vector3D(nmsPos.getX(), nmsPos.getY(), nmsPos.getZ());
            BlockWrapper block = p.world().getBlock(pos);
            if (block == null) {
                return null;
            }

            Direction dir;
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
                default:
                    return null;
            }

            return new BlockDigEvent(p, pos, dir, digType, block);
        }
        ItemWrapper itemStack = p.inventory.hand();
        if (itemStack == null) {
            return null;
        }

        return new ItemInteractEvent(p, interactType, itemStack);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInBlockPlace packet) {
        BlockPosition nmsPos = packet.a();
        int x = nmsPos.getX();
        int y = nmsPos.getY();
        int z = nmsPos.getZ();

        Vector3D targetPos = new Vector3D(x, y, z);

        Direction dir;
        switch (packet.getFace()) {
            case 0:
                dir = Direction.DOWN;
                y -= 1;
                break;
            case 1:
                dir = Direction.UP;
                y += 1;
                break;
            case 2:
                dir = Direction.NORTH;
                z -= 1;
                break;
            case 3:
                dir = Direction.SOUTH;
                z += 1;
                break;
            case 4:
                dir = Direction.WEST;
                x -= 1;
                break;
            case 5:
                dir = Direction.EAST;
                x += 1;
                break;
            default:
                return null;
        }

        ItemStack itemStack = packet.getItemStack();

        Vector3D cursorPos = new Vector3D(packet.d(), packet.e(), packet.f());

        Vector3D placePos = new Vector3D(x, y, z);

        if (!targetPos.equals(INVALID_7) && !targetPos.equals(INVALID_8)) {
            if (itemStack == null || itemStack.getItem() == null) {
                // TODO: The player probably placed a null block, kick him?
                return null;
            }
            org.bukkit.Material mat = CraftMagicNumbers.getMaterial(itemStack.getItem());
            BlockInteractEvent.InteractType placeType;
            if (mat != null && mat != org.bukkit.Material.AIR) {
                placeType = BlockInteractEvent.InteractType.PLACE_BLOCK;
            } else {
                placeType = BlockInteractEvent.InteractType.INTERACT_BLOCK;
            }

            return new BlockInteractEvent(p, mat, targetPos, cursorPos, placePos, dir, placeType);
        } else {
            if (itemStack == null) {
                // Interact with nothing in hand.
                return null;
            }
            return new StartUseItemEvent(p, new ItemWrapper(itemStack));
        }
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInCloseWindow packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(1));
        try {
            packet.b(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int id = serializer.readByte();

        return new WindowCloseEvent(p, id);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInEntityAction packet) {
        EntityActionEvent.ActionType type;
        switch (packet.b()) {
            case START_SNEAKING:
                type = EntityActionEvent.ActionType.START_SNEAKING;
                break;
            case STOP_SNEAKING:
                type = EntityActionEvent.ActionType.STOP_SNEAKING;
                break;
            case STOP_SLEEPING:
                type = EntityActionEvent.ActionType.STOP_SLEEPING;
                break;
            case START_SPRINTING:
                type = EntityActionEvent.ActionType.START_SPRINTING;
                break;
            case STOP_SPRINTING:
                type = EntityActionEvent.ActionType.STOP_SPRINTING;
                break;
            case RIDING_JUMP:
                type = EntityActionEvent.ActionType.RIDING_JUMP;
                break;
            default:
                return null;
        }
        int jumpBoost = packet.c();

        return new EntityActionEvent(p, type, jumpBoost);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInFlying.PacketPlayInLook packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(9));
        try {
            packet.b(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Location pos = p.physics.position;
        float yaw = serializer.readFloat();
        float pitch = serializer.readFloat();
        boolean onGround = serializer.readUnsignedByte() != 0;

        return new MoveEvent(p, new Location(p.world(), pos.x, pos.y, pos.z, yaw, pitch), onGround, true, false);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInFlying.PacketPlayInPosition packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(25));
        try {
            packet.b(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Location pos = p.physics.position;
        double x = serializer.readDouble();
        double y = serializer.readDouble();
        double z = serializer.readDouble();
        boolean onGround = serializer.readUnsignedByte() != 0;

        return new MoveEvent(p, new Location(p.world(), x, y, z, pos.yaw, pos.pitch), onGround, false, true);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInFlying.PacketPlayInPositionLook packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(33));
        try {
            packet.b(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        double x = serializer.readDouble();
        double y = serializer.readDouble();
        double z = serializer.readDouble();
        float yaw = serializer.readFloat();
        float pitch = serializer.readFloat();
        boolean onGround = serializer.readUnsignedByte() != 0;

        return new MoveEvent(p, new Location(p.world(), x, y, z, yaw, pitch), onGround, true, true);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInHeldItemSlot packet) {
        int heldItemSlot = packet.a();

        return new HeldItemEvent(p, heldItemSlot);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInUseEntity packet) {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        try {
            packet.b(serializer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int entityId = serializer.e();
        EntityInteractEvent.InteractType type;
        switch (serializer.a(PacketPlayInUseEntity.EnumEntityUseAction.class)) {
            case INTERACT:
                type = EntityInteractEvent.InteractType.INTERACT;
                break;
            case ATTACK:
                type = EntityInteractEvent.InteractType.ATTACK;
                break;
            case INTERACT_AT:
                type = EntityInteractEvent.InteractType.INTERACT_AT;
                break;
            default:
                return null;
        }
        Vector3D cursorPos = null;
        if (type == EntityInteractEvent.InteractType.INTERACT_AT) {
            cursorPos = new Vector3D(serializer.readFloat(), serializer.readFloat(), serializer.readFloat());
        }

        Entity nmsEntity = packet.a(p.world().worldServer);
        EntityWrapper entity = nmsEntity == null ? null : new EntityWrapper(nmsEntity);

        return new EntityInteractEvent(p, entityId, type, cursorPos, entity);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayInKeepAlive packet) {
        return new KeepaliveRespondEvent(p, packet.a());
    }

    public static Event<?> sent(HPlayer p, Object packet) throws IOException {
        if (packet instanceof PacketPlayOutPosition) {
            return toEvent(p, (PacketPlayOutPosition) packet);
        } else if (packet instanceof PacketPlayOutKeepAlive) {
            return toEvent(p, (PacketPlayOutKeepAlive) packet);
        } else if (packet instanceof PacketPlayOutUpdateAttributes) {
            return toEvent(p, (PacketPlayOutUpdateAttributes) packet);
        } else if (packet instanceof PacketPlayOutEntityVelocity) {
            return toEvent(p, (PacketPlayOutEntityVelocity) packet);
        } else if (packet instanceof PacketPlayOutEntityMetadata) {
            return toEvent(p, (PacketPlayOutEntityMetadata) packet);
        } else if (packet instanceof PacketPlayOutAbilities) {
            return toEvent(p, (PacketPlayOutAbilities) packet);
        }
        return null;
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayOutPosition packet) throws IOException {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        packet.b(serializer);
        double x = serializer.readDouble();
        double y = serializer.readDouble();
        double z = serializer.readDouble();
        float yaw = serializer.readFloat();
        float pitch = serializer.readFloat();
        Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> relMoveFlags = PacketPlayOutPosition.EnumPlayerTeleportFlags.a(serializer.readUnsignedByte());
        return new TeleportEvent(p, x, y, z, yaw, pitch);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayOutKeepAlive packet) throws IOException {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        packet.b(serializer);
        int id = serializer.e();
        return new KeepaliveRequestEvent(p, id);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayOutUpdateAttributes packet) throws IOException {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        packet.b(serializer);
        int id = serializer.e();
        if (id != p.nms.getId()) {
            return null;
        }
        List<AttributeEvent.AttributeSnapshot> snapshots = new ArrayList<>();
        int snapshotSize = serializer.readInt();
        for (int i = 0; i < snapshotSize; i++) {
            String key = serializer.c(64);
            double baseValue = serializer.readDouble();
            int modifierSize = serializer.e();
            List<AttributeEvent.AttributeModifier> modifiers = new ArrayList<>();
            for (int j = 0; j < modifierSize; j++) {
                UUID uuid = serializer.g();
                double value = serializer.readDouble();
                byte operation = serializer.readByte();
                modifiers.add(new AttributeEvent.AttributeModifier(uuid, value, operation));
            }
            snapshots.add(new AttributeEvent.AttributeSnapshot(key, baseValue, modifiers));
        }
        return new AttributeEvent(p, snapshots);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayOutEntityVelocity packet) throws IOException {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        packet.b(serializer);
        int id = serializer.e();
        if (id != p.nms.getId()) {
            return null;
        }
        float x = serializer.readShort() / 8000F;
        float y = serializer.readShort() / 8000F;
        float z = serializer.readShort() / 8000F;
        return new VelocityEvent(p, x, y, z);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayOutEntityMetadata packet) throws IOException {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        packet.b(serializer);
        int id = serializer.e();
        List<DataWatcher.WatchableObject> metadata = DataWatcher.b(serializer);
        if (metadata == null) {
            return null;
        }
        return new MetaEvent(p, id, metadata);
    }

    private static Event<?> toEvent(HPlayer p, PacketPlayOutAbilities packet) throws IOException {
        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        packet.b(serializer);
        byte b = serializer.readByte();
        boolean invulnerable = (b & 1) > 0;
        boolean flying = (b & 2) > 0;
        boolean canFly = (b & 4) > 0;
        boolean inCreative = (b & 8) > 0;

        float flyingSpeed = serializer.readFloat();
        float walkingSpeed = serializer.readFloat();

        return new SAbilitiesEvent(p, invulnerable, flying, canFly, inCreative, flyingSpeed, walkingSpeed);
    }
}