package xyz.hstudio.horizon.bukkit.compat.v1_8_R3;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.compat.Bot;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.util.Location;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Bot_v1_8_R3 extends EntityPlayer implements Bot {

    private final HoriPlayer receiver;
    // Cache the object to reduce the usage of ram.
    private Object packetCache = null;

    public Bot_v1_8_R3(final MinecraftServer minecraftServer, final WorldServer worldServer, final GameProfile gameProfile, final PlayerInteractManager playerInteractManager, final HoriPlayer receiver, final Location location) {
        super(minecraftServer, worldServer, gameProfile, playerInteractManager);
        this.receiver = receiver;
        // Init position.
        this.setLocation(location.x, location.y, location.z, location.yaw, location.pitch);
    }

    public static Bot_v1_8_R3 create(final HoriPlayer player, final String name, final Location location) {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getHandle().getServer();
        WorldServer worldServer = ((CraftWorld) player.player.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)), name);
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
        return new Bot_v1_8_R3(minecraftServer, worldServer, gameProfile, playerInteractManager, player, location);
    }

    @Override
    public void spawn() {
        packetCache = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this);
        this.receiver.sendPacket(packetCache);
        packetCache = new PacketPlayOutNamedEntitySpawn(this);
        this.receiver.sendPacket(packetCache);
    }

    @Override
    public void updatePing(final int ping) {
        this.ping = ping;
        packetCache = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, this);
        this.receiver.sendPacket(packetCache);
    }

    @Override
    public void updateHealth(final float health) {
        this.datawatcher.watch(6, health);
        packetCache = new PacketPlayOutEntityMetadata(getId(), this.getDataWatcher(), false);
        this.receiver.sendPacket(packetCache);
        packetCache = new PacketPlayOutAnimation(this, 1);
    }

    @Override
    public void swing() {
        packetCache = new PacketPlayOutAnimation(this, 0);
        this.receiver.sendPacket(packetCache);
    }

    @Override
    public void setSneak(final boolean sneak) {
        this.setSneaking(sneak);
        packetCache = new PacketPlayOutEntityMetadata(getId(), this.getDataWatcher(), false);
        this.receiver.sendPacket(packetCache);
    }

    @Override
    public void setSprint(final boolean sprint) {
        this.setSprinting(sprint);
        packetCache = new PacketPlayOutEntityMetadata(getId(), this.getDataWatcher(), false);
        this.receiver.sendPacket(packetCache);
    }

    @Override
    public void move(final Location to, final boolean onGround) {
        // Wtf, there's no even a method to update entity's position.
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.locX = to.x;
        this.locY = to.y;
        this.locZ = to.z;
        this.yaw = to.yaw;
        this.pitch = to.pitch;

        Vector relativePos = new Vector(this.locX - this.lastX, this.locY - this.lastY, this.locZ - this.lastZ);
        double posLength = relativePos.length();
        // 1.8.8: * 32
        // 1.12+: * 4096
        byte deltaX = (byte) (relativePos.getX() * 32);
        byte deltaY = (byte) (relativePos.getY() * 32);
        byte deltaZ = (byte) (relativePos.getZ() * 32);

        Vector relativeRot = new Vector(this.yaw - this.lastYaw, 0, this.pitch - this.lastPitch);
        double rotLength = relativeRot.length();
        // New angle, not delta
        byte newYaw = (byte) (NumberConversions.floor(this.yaw) * 256F / 360F);
        byte newPitch = (byte) (NumberConversions.floor(this.pitch) * 256F / 360F);

        if (posLength == 0 && rotLength == 0) {
            return;
        }
        // 1.8.8: 4
        // 1.12+: 8
        if (posLength > 4) {
            packetCache = new PacketPlayOutEntityTeleport(this);
            this.receiver.sendPacket(packetCache);
            return;
        }

        if (posLength > 0) {
            packetCache = rotLength > 0 ?
                    new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(getId(), deltaX, deltaY, deltaZ, newYaw, newPitch, onGround) :
                    new PacketPlayOutEntity.PacketPlayOutRelEntityMove(getId(), deltaX, deltaY, deltaZ, onGround);
        } else if (rotLength > 0) {
            packetCache = new PacketPlayOutEntity.PacketPlayOutEntityLook(getId(), newYaw, newPitch, onGround);
        }

        this.receiver.sendPacket(packetCache);

        if (this.yaw != this.lastYaw) {
            packetCache = new PacketPlayOutEntityHeadRotation(this, newYaw);
            this.receiver.sendPacket(packetCache);
        }
    }
}