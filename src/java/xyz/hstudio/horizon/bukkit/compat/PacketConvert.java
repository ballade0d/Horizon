package xyz.hstudio.horizon.bukkit.compat;

import lombok.Getter;
import xyz.hstudio.horizon.bukkit.compat.v1_12_R1.PacketConvert_v1_12_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_13_R2.PacketConvert_v1_13_R2;
import xyz.hstudio.horizon.bukkit.compat.v1_14_R1.PacketConvert_v1_14_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_15_R1.PacketConvert_v1_15_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_8_R3.PacketConvert_v1_8_R3;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.util.Version;

public abstract class PacketConvert {

    @Getter
    private static PacketConvert inst;

    public static void init() {
        switch (Version.VERSION) {
            case v1_8:
                inst = new PacketConvert_v1_8_R3();
                break;
            case v1_12:
                inst = new PacketConvert_v1_12_R1();
                break;
            case v1_13:
                inst = new PacketConvert_v1_13_R2();
                break;
            case v1_14:
                inst = new PacketConvert_v1_14_R1();
                break;
            case v1_15:
                inst = new PacketConvert_v1_15_R1();
                break;
        }
    }

    public abstract Event convertIn(final HoriPlayer player, final Object packet);

    public abstract Event convertOut(final HoriPlayer player, final Object packet);
}