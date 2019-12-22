package xyz.hstudio.horizon.bukkit.compat;

import lombok.Getter;
import xyz.hstudio.horizon.bukkit.compat.v1_8_R3.PacketConvert_v1_8_R3;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;

public abstract class PacketConvert {

    @Getter
    private static PacketConvert inst;

    public static void init() {
        inst = new PacketConvert_v1_8_R3();
    }

    public abstract Event convertIn(final HoriPlayer player, final Object packet);

    public abstract Event convertOut(final HoriPlayer player, final Object packet);
}