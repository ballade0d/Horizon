package xyz.hstudio.horizon.module.checks;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.DataWatcher;
import org.bukkit.entity.*;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.outbound.MetaEvent;
import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.node.HealthTagNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.enums.Version;

import java.io.IOException;
import java.util.List;

public class HealthTag extends Module<Data, HealthTagNode> {

    private final Impl impl;

    public HealthTag() {
        super(ModuleType.HealthTag, new HealthTagNode());
        switch (Version.VERSION) {
            case v1_8_R3:
                impl = new Impl_v1_8_R3();
                break;
            case v1_12_R1:
                impl = new Impl_v1_12_R1();
                break;
            case v1_13_R2:
                impl = new Impl_v1_13_R2();
                break;
            case v1_14_R1:
                impl = new Impl_v1_14_R1();
                break;
            case v1_15_R1:
                impl = new Impl_v1_15_R1();
                break;
            default:
                impl = null;
                break;
        }
    }

    @Override
    public Data getData(final HoriPlayer player) {
        return null;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final Data data, final HealthTagNode config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final Data data, final HealthTagNode config) {
        if (event instanceof MetaEvent) {
            try {
                event.rawPacket = this.impl.spoofHealth(player, event.rawPacket);
            } catch (Exception ignore) {
            }
        }
    }

    private interface Impl {
        Object spoofHealth(final HoriPlayer player, final Object packet) throws IOException;
    }

    private static class Impl_v1_8_R3 implements Impl {
        @Override
        public Object spoofHealth(final HoriPlayer player, final Object packet) throws IOException {
            net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata nms = (net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata) packet;
            net.minecraft.server.v1_8_R3.PacketDataSerializer serializer = new net.minecraft.server.v1_8_R3.PacketDataSerializer(Unpooled.buffer(64));
            nms.b(serializer);

            int id = serializer.e();
            List<DataWatcher.WatchableObject> objects = net.minecraft.server.v1_8_R3.DataWatcher.b(serializer);

            if (id == player.player.getEntityId() || objects == null) {
                return packet;
            }
            net.minecraft.server.v1_8_R3.Entity entity = ((org.bukkit.craftbukkit.v1_8_R3.CraftWorld) player.player.getWorld()).getHandle().a(id);
            if (entity == null) {
                return packet;
            }
            org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity craftEntity = entity.getBukkitEntity();
            if (craftEntity instanceof Wither || craftEntity instanceof EnderDragon) {
                return packet;
            }
            if (!(craftEntity instanceof HumanEntity) && !(craftEntity instanceof Monster) && !(craftEntity instanceof Animals) && !(craftEntity instanceof Golem) && !(craftEntity instanceof WaterMob) && !(craftEntity instanceof Villager)) {
                return packet;
            }
            if (player.getVehicle() != null && player.getVehicle().getUniqueId().equals(craftEntity.getUniqueId())) {
                return packet;
            }

            boolean reset = false;

            for (net.minecraft.server.v1_8_R3.DataWatcher.WatchableObject object : objects) {
                if (object.b() instanceof Float && object.c() == 3 && (float) object.b() > 0.0F) {
                    object.a(Float.NaN);
                    reset = true;
                    break;
                }
            }

            if (!reset) {
                return packet;
            }

            serializer.clear();
            serializer.b(id);
            net.minecraft.server.v1_8_R3.DataWatcher.a(objects, serializer);

            net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata newPacket = new net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata();
            newPacket.a(serializer);
            return newPacket;
        }
    }

    private static class Impl_v1_12_R1 implements Impl {
        @Override
        public Object spoofHealth(final HoriPlayer player, final Object packet) throws IOException {
            net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata nms = (net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata) packet;
            net.minecraft.server.v1_12_R1.PacketDataSerializer serializer = new net.minecraft.server.v1_12_R1.PacketDataSerializer(Unpooled.buffer(64));
            nms.b(serializer);

            int id = serializer.g();
            List<net.minecraft.server.v1_12_R1.DataWatcher.Item<?>> objects = net.minecraft.server.v1_12_R1.DataWatcher.b(serializer);

            if (id == player.player.getEntityId() || objects == null) {
                return packet;
            }
            net.minecraft.server.v1_12_R1.Entity entity = ((org.bukkit.craftbukkit.v1_12_R1.CraftWorld) player.player.getWorld()).getHandle().getEntity(id);
            if (entity == null) {
                return packet;
            }
            org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity craftEntity = entity.getBukkitEntity();
            if (craftEntity instanceof Wither || craftEntity instanceof EnderDragon) {
                return packet;
            }
            if (!(craftEntity instanceof HumanEntity) && !(craftEntity instanceof Monster) && !(craftEntity instanceof Animals) && !(craftEntity instanceof Golem) && !(craftEntity instanceof WaterMob) && !(craftEntity instanceof Villager)) {
                return packet;
            }
            if (player.player.getVehicle() != null && player.player.getVehicle().getUniqueId().equals(craftEntity.getUniqueId())) {
                return packet;
            }

            boolean reset = false;

            for (net.minecraft.server.v1_12_R1.DataWatcher.Item object : objects) {
                if (object.b() instanceof Float && object.a().a() == 7 && (float) object.b() > 0.0F) {
                    object.a(Float.NaN);
                    reset = true;
                    break;
                }
            }

            if (!reset) {
                return packet;
            }

            serializer.clear();
            serializer.b(id);
            net.minecraft.server.v1_12_R1.DataWatcher.a(objects, serializer);

            net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata newPacket = new net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata();
            newPacket.a(serializer);
            return newPacket;
        }
    }

    private static class Impl_v1_13_R2 implements Impl {
        @Override
        public Object spoofHealth(final HoriPlayer player, final Object packet) throws IOException {
            net.minecraft.server.v1_13_R2.PacketPlayOutEntityMetadata nms = (net.minecraft.server.v1_13_R2.PacketPlayOutEntityMetadata) packet;
            net.minecraft.server.v1_13_R2.PacketDataSerializer serializer = new net.minecraft.server.v1_13_R2.PacketDataSerializer(Unpooled.buffer(64));
            nms.b(serializer);

            int id = serializer.g();
            List<net.minecraft.server.v1_13_R2.DataWatcher.Item<?>> objects = net.minecraft.server.v1_13_R2.DataWatcher.b(serializer);

            if (id == player.player.getEntityId() || objects == null) {
                return packet;
            }
            net.minecraft.server.v1_13_R2.Entity entity = ((org.bukkit.craftbukkit.v1_13_R2.CraftWorld) player.player.getWorld()).getHandle().getEntity(id);
            if (entity == null) {
                return packet;
            }
            org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity craftEntity = entity.getBukkitEntity();
            if (craftEntity instanceof Wither || craftEntity instanceof EnderDragon) {
                return packet;
            }
            if (!(craftEntity instanceof HumanEntity) && !(craftEntity instanceof Monster) && !(craftEntity instanceof Animals) && !(craftEntity instanceof Golem) && !(craftEntity instanceof WaterMob) && !(craftEntity instanceof Villager)) {
                return packet;
            }
            if (player.player.getVehicle() != null && player.player.getVehicle().getUniqueId().equals(craftEntity.getUniqueId())) {
                return packet;
            }

            boolean reset = false;

            for (net.minecraft.server.v1_13_R2.DataWatcher.Item object : objects) {
                if (object.b() instanceof Float && object.a().a() == 7 && (float) object.b() > 0.0F) {
                    object.a(Float.NaN);
                    reset = true;
                    break;
                }
            }

            if (!reset) {
                return packet;
            }

            serializer.clear();
            serializer.d(id);
            net.minecraft.server.v1_13_R2.DataWatcher.a(objects, serializer);

            net.minecraft.server.v1_13_R2.PacketPlayOutEntityMetadata newPacket = new net.minecraft.server.v1_13_R2.PacketPlayOutEntityMetadata();
            newPacket.a(serializer);
            return newPacket;
        }
    }

    private static class Impl_v1_14_R1 implements Impl {
        @Override
        public Object spoofHealth(final HoriPlayer player, final Object packet) throws IOException {
            net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata nms = (net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata) packet;
            net.minecraft.server.v1_14_R1.PacketDataSerializer serializer = new net.minecraft.server.v1_14_R1.PacketDataSerializer(Unpooled.buffer(64));
            nms.b(serializer);

            int id = serializer.i();
            List<net.minecraft.server.v1_14_R1.DataWatcher.Item<?>> objects = net.minecraft.server.v1_14_R1.DataWatcher.b(serializer);

            if (id == player.player.getEntityId() || objects == null) {
                return packet;
            }
            net.minecraft.server.v1_14_R1.Entity entity = ((org.bukkit.craftbukkit.v1_14_R1.CraftWorld) player.player.getWorld()).getHandle().getEntity(id);
            if (entity == null) {
                return packet;
            }
            org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity craftEntity = entity.getBukkitEntity();
            if (craftEntity instanceof Wither || craftEntity instanceof EnderDragon) {
                return packet;
            }
            if (!(craftEntity instanceof HumanEntity) && !(craftEntity instanceof Monster) && !(craftEntity instanceof Animals) && !(craftEntity instanceof Golem) && !(craftEntity instanceof WaterMob) && !(craftEntity instanceof Villager)) {
                return packet;
            }
            if (player.player.getVehicle() != null && player.player.getVehicle().getUniqueId().equals(craftEntity.getUniqueId())) {
                return packet;
            }

            boolean reset = false;

            for (net.minecraft.server.v1_14_R1.DataWatcher.Item object : objects) {
                if (object.b() instanceof Float && object.a().a() == 7 && (float) object.b() > 0.0F) {
                    object.a(Float.NaN);
                    reset = true;
                    break;
                }
            }

            if (!reset) {
                return packet;
            }

            serializer.clear();
            serializer.d(id);
            net.minecraft.server.v1_14_R1.DataWatcher.a(objects, serializer);

            net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata newPacket = new net.minecraft.server.v1_14_R1.PacketPlayOutEntityMetadata();
            newPacket.a(serializer);
            return newPacket;
        }
    }

    private static class Impl_v1_15_R1 implements Impl {
        @Override
        public Object spoofHealth(final HoriPlayer player, final Object packet) throws IOException {
            net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata nms = (net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata) packet;
            net.minecraft.server.v1_15_R1.PacketDataSerializer serializer = new net.minecraft.server.v1_15_R1.PacketDataSerializer(Unpooled.buffer(64));
            nms.b(serializer);

            int id = serializer.i();
            List<net.minecraft.server.v1_15_R1.DataWatcher.Item<?>> objects = net.minecraft.server.v1_15_R1.DataWatcher.a(serializer);

            if (id == player.player.getEntityId() || objects == null) {
                return packet;
            }
            net.minecraft.server.v1_15_R1.Entity entity = ((org.bukkit.craftbukkit.v1_15_R1.CraftWorld) player.player.getWorld()).getHandle().getEntity(id);
            if (entity == null) {
                return packet;
            }
            org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity craftEntity = entity.getBukkitEntity();
            if (craftEntity instanceof Wither || craftEntity instanceof EnderDragon) {
                return packet;
            }
            if (!(craftEntity instanceof HumanEntity) && !(craftEntity instanceof Monster) && !(craftEntity instanceof Animals) && !(craftEntity instanceof Golem) && !(craftEntity instanceof WaterMob) && !(craftEntity instanceof Villager)) {
                return packet;
            }
            if (player.player.getVehicle() != null && player.player.getVehicle().getUniqueId().equals(craftEntity.getUniqueId())) {
                return packet;
            }

            boolean reset = false;

            for (net.minecraft.server.v1_15_R1.DataWatcher.Item object : objects) {
                if (object.b() instanceof Float && object.a().a() == 7 && (float) object.b() > 0.0F) {
                    object.a(Float.NaN);
                    reset = true;
                    break;
                }
            }

            if (!reset) {
                return packet;
            }

            serializer.clear();
            serializer.d(id);
            net.minecraft.server.v1_15_R1.DataWatcher.a(objects, serializer);

            net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata newPacket = new net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata();
            newPacket.a(serializer);
            return newPacket;
        }
    }
}