package xyz.hstudio.horizon.bukkit.compat;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.bukkit.compat.v1_8_R3.McAccess_v1_8_R3;

public abstract class McAccess {

    @Getter
    private static McAccess inst;

    public static void init() {
        inst = new McAccess_v1_8_R3();
    }

    public abstract ChannelPipeline getPipeline(final Player player);

    public abstract float sin(final float v);

    public abstract float cos(final float v);

    public abstract boolean isAccumulated(final Player player);
}