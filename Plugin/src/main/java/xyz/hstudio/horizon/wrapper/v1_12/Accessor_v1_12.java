package xyz.hstudio.horizon.wrapper.v1_12;

import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_12_R1.MathHelper;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.wrapper.AccessorBase;

public class Accessor_v1_12 extends AccessorBase {

    @Override
    public float sin(float v) {
        return MathHelper.sin(v);
    }

    @Override
    public float cos(float v) {
        return MathHelper.cos(v);
    }

    @Override
    public ChannelPipeline getPipeline(HPlayer p) {
        return ((CraftPlayer) p.getBPlayer()).getHandle().playerConnection.networkManager.channel.pipeline();
    }
}