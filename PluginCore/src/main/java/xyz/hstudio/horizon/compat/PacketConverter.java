package xyz.hstudio.horizon.compat;

import xyz.hstudio.horizon.compat.v1_12_R1.PacketConverter_v1_12_R1;
import xyz.hstudio.horizon.compat.v1_13_R2.PacketConverter_v1_13_R2;
import xyz.hstudio.horizon.compat.v1_8_R3.PacketConverter_v1_8_R3;
import xyz.hstudio.horizon.util.enums.Version;

public final class PacketConverter {

    private PacketConverter() {
    }

    public static final IPacketConverter INSTANCE;

    static {
        switch (Version.VERSION) {
            case v1_8_R3:
                INSTANCE = new PacketConverter_v1_8_R3();
                break;
            case v1_12_R1:
                INSTANCE = new PacketConverter_v1_12_R1();
                break;
            case v1_13_R2:
                INSTANCE = new PacketConverter_v1_13_R2();
                break;
            default:
                INSTANCE = null;
                break;
        }
    }
}