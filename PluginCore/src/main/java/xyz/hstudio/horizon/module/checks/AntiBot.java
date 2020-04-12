package xyz.hstudio.horizon.module.checks;

import io.netty.buffer.Unpooled;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.outbound.UpdatePosEvent;
import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.node.AntiBotNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.enums.Version;

public class AntiBot extends Module<Data, AntiBotNode> {

    private final Impl impl;

    public AntiBot() {
        super(ModuleType.AntiBot, new AntiBotNode());
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
    public void cancel(final Event event, final String type, final HoriPlayer player, final Data data, final AntiBotNode config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final Data data, final AntiBotNode config) {
        if (event instanceof UpdatePosEvent) {
            UpdatePosEvent e = (UpdatePosEvent) event;
            try {
                e.rawPacket = this.impl.spoofGround(player, event.rawPacket);
            } catch (Exception ignore) {
            }
        }
    }

    private interface Impl {
        Object spoofGround(final HoriPlayer player, final Object packet) throws Exception;
    }

    private static class Impl_v1_8_R3 implements Impl {
        @Override
        public Object spoofGround(final HoriPlayer player, final Object packet) throws Exception {
            net.minecraft.server.v1_8_R3.PacketPlayOutEntity nms = (net.minecraft.server.v1_8_R3.PacketPlayOutEntity) packet;
            net.minecraft.server.v1_8_R3.PacketDataSerializer serializer = new net.minecraft.server.v1_8_R3.PacketDataSerializer(Unpooled.buffer());

            nms.b(serializer);
            if (nms instanceof net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMove) {
                serializer.setBoolean(5, true);
            } else if (nms instanceof net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook) {
                serializer.setBoolean(4, true);
            } else if (nms instanceof net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook) {
                serializer.setBoolean(7, true);
            }
            nms.a(serializer);

            return nms;
        }
    }

    private static class Impl_v1_12_R1 implements Impl {
        @Override
        public Object spoofGround(final HoriPlayer player, final Object packet) throws Exception {
            net.minecraft.server.v1_12_R1.PacketPlayOutEntity nms = (net.minecraft.server.v1_12_R1.PacketPlayOutEntity) packet;
            net.minecraft.server.v1_12_R1.PacketDataSerializer serializer = new net.minecraft.server.v1_12_R1.PacketDataSerializer(Unpooled.buffer());

            nms.b(serializer);
            if (nms instanceof net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMove) {
                serializer.setBoolean(8, true);
            } else if (nms instanceof net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutEntityLook) {
                serializer.setBoolean(4, true);
            } else if (nms instanceof net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook) {
                serializer.setBoolean(10, true);
            }
            nms.a(serializer);

            return nms;
        }
    }

    private static class Impl_v1_13_R2 implements Impl {
        @Override
        public Object spoofGround(final HoriPlayer player, final Object packet) throws Exception {
            net.minecraft.server.v1_13_R2.PacketPlayOutEntity nms = (net.minecraft.server.v1_13_R2.PacketPlayOutEntity) packet;
            net.minecraft.server.v1_13_R2.PacketDataSerializer serializer = new net.minecraft.server.v1_13_R2.PacketDataSerializer(Unpooled.buffer());

            nms.b(serializer);
            if (nms instanceof net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutRelEntityMove) {
                serializer.setBoolean(8, true);
            } else if (nms instanceof net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutEntityLook) {
                serializer.setBoolean(4, true);
            } else if (nms instanceof net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook) {
                serializer.setBoolean(10, true);
            }
            nms.a(serializer);

            return nms;
        }
    }

    private static class Impl_v1_14_R1 implements Impl {
        @Override
        public Object spoofGround(final HoriPlayer player, final Object packet) throws Exception {
            net.minecraft.server.v1_14_R1.PacketPlayOutEntity nms = (net.minecraft.server.v1_14_R1.PacketPlayOutEntity) packet;
            net.minecraft.server.v1_14_R1.PacketDataSerializer serializer = new net.minecraft.server.v1_14_R1.PacketDataSerializer(Unpooled.buffer());

            nms.b(serializer);
            if (nms instanceof net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMove) {
                serializer.setBoolean(8, true);
            } else if (nms instanceof net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutEntityLook) {
                serializer.setBoolean(4, true);
            } else if (nms instanceof net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook) {
                serializer.setBoolean(10, true);
            }
            nms.a(serializer);

            return nms;
        }
    }

    private static class Impl_v1_15_R1 implements Impl {
        @Override
        public Object spoofGround(final HoriPlayer player, final Object packet) throws Exception {
            net.minecraft.server.v1_15_R1.PacketPlayOutEntity nms = (net.minecraft.server.v1_15_R1.PacketPlayOutEntity) packet;
            net.minecraft.server.v1_15_R1.PacketDataSerializer serializer = new net.minecraft.server.v1_15_R1.PacketDataSerializer(Unpooled.buffer());

            nms.b(serializer);
            if (nms instanceof net.minecraft.server.v1_15_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMove) {
                serializer.setBoolean(8, true);
            } else if (nms instanceof net.minecraft.server.v1_15_R1.PacketPlayOutEntity.PacketPlayOutEntityLook) {
                serializer.setBoolean(4, true);
            } else if (nms instanceof net.minecraft.server.v1_15_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook) {
                serializer.setBoolean(10, true);
            }
            nms.a(serializer);

            return nms;
        }
    }
}