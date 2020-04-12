package xyz.hstudio.horizon.compat.v1_8_R3;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.compat.IBot;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.RandomUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Bot_v1_8_R3 implements IBot {

    private EntityPlayer entityPlayer;
    private final boolean realName;
    private final long spawnTime;

    public Bot_v1_8_R3(final Player player, final boolean realisticName) {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();

        List<Player> players = Bukkit.getOnlinePlayers()
                .stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .collect(Collectors.toList());
        this.realName = realisticName && players.size() > 0;
        String name = this.realName ?
                players.get(ThreadLocalRandom.current().nextInt(players.size())).getName() :
                RandomStringUtils.randomAlphanumeric(8);

        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(uuid, name);
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
        EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, profile, playerInteractManager);
        Location backLoc = player.getLocation();
        entityPlayer.listName = CraftChatMessage.fromString("§f" + name)[0];
        entityPlayer.setInvisible(false);
        entityPlayer.setLocation(backLoc.getX(), backLoc.getY(), backLoc.getZ(), backLoc.getYaw(), backLoc.getPitch());
        this.entityPlayer = entityPlayer;

        this.spawnTime = System.currentTimeMillis();
    }

    @Override
    public void spawn(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        player.sendPacket(packet);
        packet = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        player.sendPacket(packet);
    }

    @Override
    public void removeFromTabList(final HoriPlayer player) {
        Packet<?> packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        player.sendPacket(packet);
    }

    @Override
    public void despawn(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        player.sendPacket(packet);
        packet = new PacketPlayOutEntityDestroy(entityPlayer.getId());
        player.sendPacket(packet);
    }

    @Override
    public void updatePing(final HoriPlayer player) {
        Packet<?> packet;
        entityPlayer.ping = RandomUtils.randomBoundaryInt(100, 500);
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, entityPlayer);
        player.sendPacket(packet);
    }

    @Override
    public void setSneaking(final boolean sneaking) {
        entityPlayer.setSneaking(sneaking);
    }

    @Override
    public void setSprinting(final boolean sprinting) {
        entityPlayer.setSprinting(sprinting);
    }

    @Override
    public void updateStatus(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutEntityMetadata(entityPlayer.getId(), entityPlayer.getDataWatcher(), true);
        player.sendPacket(packet);
    }

    @Override
    public void swingArm(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutAnimation(entityPlayer, 0);
        player.sendPacket(packet);
    }

    @Override
    public void damage(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutEntityStatus(entityPlayer, (byte) 2);
        player.sendPacket(packet);
    }

    @Override
    public void move(final double x, final double y, final double z, final float yaw, final float pitch, final HoriPlayer player) {
        Packet<?> packet;
        entityPlayer.setLocation(x, y, z, yaw, pitch);
        packet = new PacketPlayOutEntityTeleport(entityPlayer);
        player.sendPacket(packet);
        packet = new PacketPlayOutEntity.PacketPlayOutEntityLook(entityPlayer.getId(), (byte) yaw, (byte) pitch, ThreadLocalRandom.current().nextBoolean());
        player.sendPacket(packet);
        packet = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) yaw);
        player.sendPacket(packet);
    }

    @Override
    public void setArmor(final HoriPlayer player) {
        Packet<?> packet;
        ItemStack itemStack;
        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(HELMET[ThreadLocalRandom.current().nextInt(HELMET.length)]));
        packet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 4, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(CHESTPLATE[ThreadLocalRandom.current().nextInt(CHESTPLATE.length)]));
        packet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 3, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(LEGGINGS[ThreadLocalRandom.current().nextInt(LEGGINGS.length)]));
        packet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 2, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(BOOTS[ThreadLocalRandom.current().nextInt(BOOTS.length)]));
        packet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 1, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(HAND[ThreadLocalRandom.current().nextInt(HAND.length)]));
        packet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 0, itemStack);
        player.sendPacket(packet);
    }

    @Override
    public int getId() {
        return entityPlayer.getId();
    }

    @Override
    public long getSpawnTime() {
        return this.spawnTime;
    }

    @Override
    public boolean isRealName() {
        return this.realName;
    }
}