package xyz.hstudio.horizon.compat.v1_12_R1;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.*;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.compat.IBot;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.RandomUtils;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Bot_v1_12_R1 implements IBot {

    private EntityPlayer nms;
    private final boolean realName;
    private final long spawnTime;

    public Bot_v1_12_R1(final Player player, final boolean realisticName) {
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
        this.nms = entityPlayer;

        this.spawnTime = System.currentTimeMillis();
    }

    @Override
    public void spawn(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nms);
        player.sendPacket(packet);
        packet = new PacketPlayOutNamedEntitySpawn(nms);
        player.sendPacket(packet);
    }

    @Override
    public void removeFromTabList(final HoriPlayer player) {
        Packet<?> packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nms);
        player.sendPacket(packet);
    }

    @Override
    public void despawn(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, nms);
        player.sendPacket(packet);
        packet = new PacketPlayOutEntityDestroy(nms.getId());
        player.sendPacket(packet);
    }

    @Override
    public void updatePing(final HoriPlayer player) {
        Packet<?> packet;
        nms.ping = RandomUtils.randomBoundaryInt(100, 500);
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, nms);
        player.sendPacket(packet);
    }

    @Override
    public void setSneaking(final boolean sneaking) {
        nms.setSneaking(sneaking);
    }

    @Override
    public void setSprinting(final boolean sprinting) {
        nms.setSprinting(sprinting);
    }

    @Override
    public void updateStatus(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutEntityMetadata(nms.getId(), nms.getDataWatcher(), true);
        player.sendPacket(packet);
    }

    @Override
    public void swingArm(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutAnimation(nms, 0);
        player.sendPacket(packet);
    }

    @Override
    public void damage(final HoriPlayer player) {
        Packet<?> packet;
        packet = new PacketPlayOutEntityStatus(nms, (byte) 2);
        player.sendPacket(packet);
    }

    @Override
    public void move(final double x, final double y, final double z, final float yaw, final float pitch, final HoriPlayer player) {
        Packet<?> packet;

        Vector3D relative = new Vector3D(x, y, z).subtract(new Vector3D(nms.locX, nms.locY, nms.locZ));

        nms.locX = x;
        nms.locY = y;
        nms.locZ = z;

        if (Math.abs(relative.x) + Math.abs(relative.y) + Math.abs(relative.z) > 8) {
            packet = new PacketPlayOutEntityTeleport(nms);
        } else {
            packet = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(nms.getId(),
                    (long) (relative.x * 4096L), (long) (relative.y * 4096L), (long) (relative.z * 4096L),
                    (byte) yaw, (byte) pitch, ThreadLocalRandom.current().nextBoolean());
        }

        player.sendPacket(packet);
        packet = new PacketPlayOutEntityHeadRotation(nms, (byte) yaw);
        player.sendPacket(packet);
    }

    @Override
    public void setArmor(final HoriPlayer player) {
        Packet<?> packet;
        ItemStack itemStack;
        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(HELMET[ThreadLocalRandom.current().nextInt(HELMET.length)]));
        packet = new PacketPlayOutEntityEquipment(nms.getId(), EnumItemSlot.HEAD, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(CHESTPLATE[ThreadLocalRandom.current().nextInt(CHESTPLATE.length)]));
        packet = new PacketPlayOutEntityEquipment(nms.getId(), EnumItemSlot.CHEST, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(LEGGINGS[ThreadLocalRandom.current().nextInt(LEGGINGS.length)]));
        packet = new PacketPlayOutEntityEquipment(nms.getId(), EnumItemSlot.LEGS, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(BOOTS[ThreadLocalRandom.current().nextInt(BOOTS.length)]));
        packet = new PacketPlayOutEntityEquipment(nms.getId(), EnumItemSlot.FEET, itemStack);
        player.sendPacket(packet);

        itemStack = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(HAND[ThreadLocalRandom.current().nextInt(HAND.length)]));
        packet = new PacketPlayOutEntityEquipment(nms.getId(), EnumItemSlot.MAINHAND, itemStack);
        player.sendPacket(packet);
    }

    @Override
    public int getId() {
        return nms.getId();
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