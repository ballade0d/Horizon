package xyz.hstudio.horizon.bukkit.compat.v1_14_R1;

import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_14_R1.MathHelper;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.bukkit.compat.McAccess;

public class McAccess_v1_14_R1 extends McAccess {

    @Override
    public ChannelPipeline getPipeline(final Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
    }

    @Override
    public float sin(final float v) {
        return MathHelper.sin(v);
    }

    @Override
    public float cos(final float v) {
        return MathHelper.cos(v);
    }

    @Override
    public boolean isAccumulated(final Player player) {
        return true;
    }
}