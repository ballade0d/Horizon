package xyz.hstudio.horizon.bukkit.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ChannelHandler extends ChannelDuplexHandler {

    public void channelRead(final ChannelHandlerContext ctx, Object packet) throws Exception {
        super.channelRead(ctx, packet);
    }

    public void write(final ChannelHandlerContext ctx, Object packet, final ChannelPromise promise) throws Exception {
        super.write(ctx, packet, promise);
    }
}