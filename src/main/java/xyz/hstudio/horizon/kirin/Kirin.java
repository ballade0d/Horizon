package xyz.hstudio.horizon.kirin;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import xyz.hstudio.horizon.Logger;

public class Kirin {

    private final byte[] clientKey = AES.generate();
    private final String licence;

    public Kirin(final String licence) throws Exception {
        this.licence = licence;
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        Bootstrap client = new Bootstrap();
        client.group(boss).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addFirst("ssl", new SSLHandler());
                pipeline.addLast("handler", new Handler());
            }
        });
        ChannelFuture future = client.connect("49.235.221.123", 8888).sync();
        future.channel().closeFuture().sync();
        boss.shutdownGracefully();
    }

    private class SSLHandler extends ChannelDuplexHandler {

        private boolean receivedPublicKey;

        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            if (receivedPublicKey) {
                super.channelRead(ctx, AES.decrypt(readByteBuf(byteBuf), clientKey));
            } else {
                ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(RSA.encrypt(clientKey, readByteBuf(byteBuf))));
            }
        }

        @Override
        public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
            if (receivedPublicKey) {
                super.write(ctx, Unpooled.wrappedBuffer(AES.encrypt((String) msg, clientKey)), promise);
            } else {
                receivedPublicKey = true;
                super.write(ctx, msg, promise);
            }
        }
    }

    private class Handler extends ChannelDuplexHandler {
        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            String info = (String) msg;
            if (info.equals("LICENCE")) {
                ctx.channel().writeAndFlush(licence);
            } else if (info.equals("EXPIRED")) {
                Logger.msg("Kirin", "Your licence is expired! Please contact the author to renew your licence.");
            } else if (info.equals("FAILED")) {
                Logger.msg("Kirin", "Unknown licence!");
            } else if (info.startsWith("OK:")) {
                // TODO: Start Premium Checks
                Logger.msg("Kirin", "Login successfully! Hello " + info.replaceFirst("OK:", "") + " :)");
            }
            super.channelRead(ctx, msg);
        }
    }

    private byte[] readByteBuf(final ByteBuf byteBuf) {
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}