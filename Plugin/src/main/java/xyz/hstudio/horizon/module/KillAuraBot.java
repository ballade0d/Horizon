package xyz.hstudio.horizon.module;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.configuration.ConfigBase;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.RandomUtils;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.Yaml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static xyz.hstudio.horizon.module.KillAuraBotCfg.*;

class KillAuraBotCfg extends ConfigBase {
    static final Yaml def = Yaml.loadConfiguration(Horizon.class.getResourceAsStream("check/kill_aura_bot.yml"));

    @LoadInfo(path = "command_only")
    public static boolean command_only;
    @LoadInfo(path = "update_interval")
    public static int update_interval;
    @LoadInfo(path = "xz_distance")
    public static double xz_distance;
    @LoadInfo(path = "y_distance")
    public static double y_distance;
    @LoadInfo(path = "offset_x")
    public static double offset_x;
    @LoadInfo(path = "offset_y")
    public static double offset_y;
    @LoadInfo(path = "offset_z")
    public static double offset_z;
    @LoadInfo(path = "show_damage")
    public static boolean show_damage;
    @LoadInfo(path = "show_swing")
    public static boolean show_swing;
    @LoadInfo(path = "show_armor")
    public static boolean show_armor;
    @LoadInfo(path = "realistic_ping")
    public static boolean realistic_ping;
    @LoadInfo(path = "async_packet")
    public static boolean async_packet;
}

public class KillAuraBot extends CheckBase {

    private static final ItemStack[] HELMET = {
            new ItemStack(CraftMagicNumbers.getItem(Material.LEATHER_HELMET)),
            new ItemStack(CraftMagicNumbers.getItem(Material.IRON_HELMET)),
            new ItemStack(CraftMagicNumbers.getItem(Material.CHAINMAIL_HELMET)),
            new ItemStack(CraftMagicNumbers.getItem(Material.DIAMOND_HELMET)),
    };
    private static final ItemStack[] CHESTPLATE = {
            new ItemStack(CraftMagicNumbers.getItem(Material.LEATHER_CHESTPLATE)),
            new ItemStack(CraftMagicNumbers.getItem(Material.IRON_CHESTPLATE)),
            new ItemStack(CraftMagicNumbers.getItem(Material.CHAINMAIL_CHESTPLATE)),
            new ItemStack(CraftMagicNumbers.getItem(Material.DIAMOND_CHESTPLATE)),
    };
    private static final ItemStack[] LEGGINGS = {
            new ItemStack(CraftMagicNumbers.getItem(Material.LEATHER_LEGGINGS)),
            new ItemStack(CraftMagicNumbers.getItem(Material.IRON_LEGGINGS)),
            new ItemStack(CraftMagicNumbers.getItem(Material.CHAINMAIL_LEGGINGS)),
            new ItemStack(CraftMagicNumbers.getItem(Material.DIAMOND_LEGGINGS)),
    };
    private static final ItemStack[] BOOTS = {
            new ItemStack(CraftMagicNumbers.getItem(Material.LEATHER_BOOTS)),
            new ItemStack(CraftMagicNumbers.getItem(Material.IRON_BOOTS)),
            new ItemStack(CraftMagicNumbers.getItem(Material.CHAINMAIL_BOOTS)),
            new ItemStack(CraftMagicNumbers.getItem(Material.DIAMOND_BOOTS)),
    };
    private static final ItemStack[] HAND = {
            new ItemStack(CraftMagicNumbers.getItem(Material.GOLDEN_APPLE)),
            new ItemStack(CraftMagicNumbers.getItem(Material.DIAMOND_SWORD)),
            new ItemStack(CraftMagicNumbers.getItem(Material.IRON_SWORD)),
            new ItemStack(CraftMagicNumbers.getItem(Material.STONE_SWORD)),
            new ItemStack(CraftMagicNumbers.getItem(Material.DIAMOND_PICKAXE)),
            new ItemStack(CraftMagicNumbers.getItem(Material.COOKED_BEEF)),
            new ItemStack(CraftMagicNumbers.getItem(Material.CHAINMAIL_BOOTS)),
            new ItemStack(CraftMagicNumbers.getItem(Material.DIAMOND_BOOTS)),
    };

    private EntityPlayer bot;

    public KillAuraBot(HPlayer p) {
        super(p, 1, 100, 100);
    }

    private static void run(Runnable runnable, boolean async) {
        if (async) {
            runnable.run();
        } else {
            inst.getSync().runSync(runnable);
        }
    }

    public static void init() {
        File file = new File(inst.getDataFolder(), "checks/kill_aura_bot.yml");
        if (!file.isFile()) {
            inst.saveResource("checks/kill_aura_bot.yml", true);
        }
        ConfigBase.load(KillAuraBotCfg.class, Yaml.loadConfiguration(file), def);
    }

    public void received(InEvent<?> event) {
        if (event instanceof EntityInteractEvent) {
            EntityInteractEvent e = (EntityInteractEvent) event;
            if (e.entity == null && bot != null && e.entityId == bot.getId()) {
                System.out.println("Hit bot!");
                return;
            }
            if (command_only || bot != null) {
                return;
            }
            bot = prepare();
            Location to = computeLoc(p.physics.position, xz_distance, y_distance);
            KillAuraBot.run(() -> {
                move(p, to.x, to.y, to.z, p.physics.position.yaw, p.physics.position.pitch);
                spawn(p);
                if (show_armor) {
                    setArmor(p);
                }
            }, async_packet);
        } else if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.teleport || bot == null) {
                return;
            }
            KillAuraBot.run(() -> {
                destroy(p);
                bot = null;
            }, async_packet);
        }
    }

    public void tickAsync(int tick) {
        if (tick % update_interval != 0 || bot == null) {
            return;
        }
        // TODO: check for command only
        Location to = computeLoc(p.physics.position, xz_distance, y_distance);

        to.add(new Vector3D(offset_x * ThreadLocalRandom.current().nextDouble(), offset_y * ThreadLocalRandom.current().nextDouble(), offset_z * ThreadLocalRandom.current().nextDouble()));

        KillAuraBot.run(() -> {
            move(p, to.x, to.y, to.z, p.physics.position.yaw, p.physics.position.pitch);

            boolean sprinting = ThreadLocalRandom.current().nextBoolean();
            if (sprinting) {
                bot.setSneaking(false);
                bot.setSprinting(true);
            } else {
                bot.setSprinting(false);
                bot.setSneaking(true);
            }

            if (show_swing) {
                swingArm(p);
            }

            if (show_damage && tick % 30 == 0) {
                damage(p);
            }

            if (realistic_ping && tick % 40 == 0) {
                updatePing(p);
            }

            updateStatus(p);
        }, async_packet);
    }

    private EntityPlayer prepare() {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) p.getWorld().bukkit()).getHandle();

        String name = RandomStringUtils.randomAlphanumeric(8);

        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(uuid, name);
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
        EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, profile, playerInteractManager);
        Location loc = computeLoc(p.physics.position, xz_distance, y_distance);
        entityPlayer.listName = CraftChatMessage.fromString("§f" + name)[0];
        entityPlayer.setInvisible(false);
        entityPlayer.setLocation(loc.x, loc.y, loc.z, loc.yaw, loc.pitch);

        return entityPlayer;
    }

    private Location computeLoc(Location pos, double xz_distance, double y_distance) {
        float yaw = (float) Math.toRadians(pos.yaw);
        return pos.plus(MathHelper.sin(yaw) * xz_distance, y_distance, MathHelper.cos(yaw) * -xz_distance);
    }

    private void spawn() {
        Packet<?> packet;
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, bot);
        p.pipeline.writeAndFlush(packet);
        packet = new PacketPlayOutNamedEntitySpawn(bot);
        p.pipeline.writeAndFlush(packet);
    }

    private void destroy() {
        Packet<?> packet;
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, bot);
        p.pipeline.writeAndFlush(packet);
        packet = new PacketPlayOutEntityDestroy(bot.getId());
        p.pipeline.writeAndFlush(packet);
    }

    private void updatePing() {
        Packet<?> packet;
        bot.ping = RandomUtils.randomBoundaryInt(100, 500);
        packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, bot);
        p.pipeline.writeAndFlush(packet);
    }

    private void updateStatus() {
        Packet<?> packet;
        packet = new PacketPlayOutEntityMetadata(bot.getId(), bot.getDataWatcher(), true);
        p.pipeline.writeAndFlush(packet);
    }

    private void swingArm() {
        Packet<?> packet;
        packet = new PacketPlayOutAnimation(bot, 0);
        p.pipeline.writeAndFlush(packet);
    }

    private void damage() {
        double oldHealth = bot.getHealth();
        double newHealth = RandomUtils.randomBoundaryInt(1, 19);
        bot.getDataWatcher().watch(6, newHealth);
        if (newHealth < oldHealth) {
            Packet<?> packet;
            packet = new PacketPlayOutEntityStatus(bot, (byte) 2);
            p.pipeline.writeAndFlush(packet);
        }
    }

    private void move(double x, double y, double z, float yaw, float pitch) {
        Packet<?> packet;

        Vector3D relative = new Vector3D(x, y, z)
                .subtract(new Vector3D(bot.locX, bot.locY, bot.locZ));

        bot.locX = x;
        bot.locY = y;
        bot.locZ = z;

        if (Math.abs(relative.x) + Math.abs(relative.y) + Math.abs(relative.z) > 4) {
            packet = new PacketPlayOutEntityTeleport(bot);
        } else {
            packet = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(bot.getId(),
                    (byte) (relative.x * 32), (byte) (relative.y * 32), (byte) (relative.z * 32),
                    (byte) yaw, (byte) pitch, ThreadLocalRandom.current().nextBoolean());
        }

        p.pipeline.writeAndFlush(packet);
        packet = new PacketPlayOutEntityHeadRotation(bot, (byte) yaw);
        p.pipeline.writeAndFlush(packet);
    }

    private void setArmor() {
        Packet<?> packet;
        ItemStack itemStack;
        itemStack = HELMET[ThreadLocalRandom.current().nextInt(HELMET.length)];
        packet = new PacketPlayOutEntityEquipment(bot.getId(), 4, itemStack);
        p.pipeline.writeAndFlush(packet);

        itemStack = CHESTPLATE[ThreadLocalRandom.current().nextInt(CHESTPLATE.length)];
        packet = new PacketPlayOutEntityEquipment(bot.getId(), 3, itemStack);
        p.pipeline.writeAndFlush(packet);

        itemStack = LEGGINGS[ThreadLocalRandom.current().nextInt(LEGGINGS.length)];
        packet = new PacketPlayOutEntityEquipment(bot.getId(), 2, itemStack);
        p.pipeline.writeAndFlush(packet);

        itemStack = BOOTS[ThreadLocalRandom.current().nextInt(BOOTS.length)];
        packet = new PacketPlayOutEntityEquipment(bot.getId(), 1, itemStack);
        p.pipeline.writeAndFlush(packet);

        itemStack = HAND[ThreadLocalRandom.current().nextInt(HAND.length)];
        packet = new PacketPlayOutEntityEquipment(bot.getId(), 0, itemStack);
        p.pipeline.writeAndFlush(packet);
    }
}
