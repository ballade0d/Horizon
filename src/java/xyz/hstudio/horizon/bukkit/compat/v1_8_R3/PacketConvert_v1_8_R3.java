package xyz.hstudio.horizon.bukkit.compat.v1_8_R3;

import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import net.minecraft.server.v1_8_R3.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import xyz.hstudio.horizon.bukkit.compat.PacketConvert;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.network.events.inbound.BadMoveEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.HeldItemEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.Hand;

public class PacketConvert_v1_8_R3 extends PacketConvert {

    @Override
    public Event convertIn(final HoriPlayer player, final Object packet) {
        if (packet instanceof PacketPlayInFlying) {
            return convertMoveEvent(player, (PacketPlayInFlying) packet);
        } else if (packet instanceof PacketPlayInUseEntity) {
            return convertInteractEntityEvent(player, (PacketPlayInUseEntity) packet);
        } else if (packet instanceof PacketPlayInHeldItemSlot) {
            return convertHeldItemEvent(player, (PacketPlayInHeldItemSlot) packet);
        }
        return null;
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
        double x = hasPos ? packet.a() : player.position.getX();
        double y = hasPos ? packet.b() : player.position.getY();
        double z = hasPos ? packet.c() : player.position.getZ();
        float yaw = hasLook ? packet.d() : player.position.getYaw();
        float pitch = hasLook ? packet.e() : player.position.getPitch();
        boolean onGround = packet.f();
        Location to = new Location(player.world, x, y, z, yaw, pitch);
        if (Math.abs(to.getX()) >= Integer.MAX_VALUE || Math.abs(to.getY()) >= Integer.MAX_VALUE || Math.abs(to.getZ()) >= Integer.MAX_VALUE ||
                Double.isNaN(to.getX()) || Double.isNaN(to.getY()) || Double.isNaN(to.getZ())) {
            return new BadMoveEvent(player, new WrappedPacket(packet));
        }
        return new MoveEvent(player, to, onGround, updatePos, updateRot, moveType, new WrappedPacket(packet));
    }

    private Event convertInteractEntityEvent(final HoriPlayer player, final PacketPlayInUseEntity packet) {
        Entity entity = packet.a(((CraftWorld) player.world).getHandle());
        if (entity == null) {
            return null;
        }
        InteractEntityEvent.InteractAction action =
                packet.a() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK ?
                        InteractEntityEvent.InteractAction.ATTACK :
                        InteractEntityEvent.InteractAction.INTERACT;
        return new InteractEntityEvent(player, action, entity.getBukkitEntity(), Hand.MAIN_HAND, new WrappedPacket(packet));
    }

    private Event convertHeldItemEvent(final HoriPlayer player, final PacketPlayInHeldItemSlot packet) {
        return new HeldItemEvent(player, packet.a(), new WrappedPacket(packet));
    }

    @Override
    public Event convertOut(final HoriPlayer player, final Object packet) {
        return null;
    }
}