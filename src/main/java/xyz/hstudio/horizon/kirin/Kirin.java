package xyz.hstudio.horizon.kirin;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.config.ConfigFile;
import xyz.hstudio.horizon.kirin.secure.AES;
import xyz.hstudio.horizon.kirin.secure.RSA;

import static xyz.hstudio.horizon.kirin.State.*;

enum State {
    STEP_1, STEP_2, STEP_3, FINISH
}

/**
 * Https-like verification system
 * <p>
 * TODO: It looks massive. Optimize it and get rid of State enum.
 */
public class Kirin {

    final byte[] clientKey = AES.generate();
    final String licence;
    State state = STEP_1;

    public Kirin(final ConfigFile config) throws Exception {
        this.licence = config.kirin_licence;

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        Bootstrap client = new Bootstrap();
        client.group(boss).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(final SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new Handler(Kirin.this));
            }
        });
        ChannelFuture future = client.connect("49.235.221.123", 8888).sync();
        future.channel().closeFuture().sync();
        boss.shutdownGracefully();
    }
}

class Handler extends ChannelDuplexHandler {

    private final Kirin kirin;

    Handler(final Kirin kirin) {
        this.kirin = kirin;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        Channel channel = ctx.channel();
        switch (kirin.state) {
            case STEP_1: {
                byte[] publicKey = read(byteBuf);

                ByteBuf respond = Unpooled.wrappedBuffer(RSA.encrypt(kirin.clientKey, publicKey));
                ctx.channel().writeAndFlush(respond);
                kirin.state = STEP_2;
                break;
            }
            case STEP_2: {
                byte[] encryptedCmd = read(byteBuf);

                String decryptedMsg = AES.decrypt(encryptedCmd, kirin.clientKey);
                if (decryptedMsg.equals("KEY")) {
                    write(kirin.licence, channel);
                    kirin.state = STEP_3;
                }
                break;
            }
            case STEP_3: {
                byte[] encryptedMsg = read(byteBuf);

                String decryptedMsg = AES.decrypt(encryptedMsg, kirin.clientKey);
                switch (decryptedMsg) {
                    case "EXPIRED":
                        Logger.msg("Kirin", "Your licence is expired! Please contact the author to renew your licence.");
                        break;
                    case "FAILED":
                        Logger.msg("Kirin", "Unknown licence!");
                        break;
                    default:
                        // TODO: START CHECKS
                        Logger.msg("Kirin", "Login successfully! Hello " + decryptedMsg + " :)");
                        break;
                }
                write("CLOSE", channel);
                kirin.state = FINISH;
                break;
            }
        }
    }

    private byte[] read(final ByteBuf byteBuf) {
        int length = byteBuf.readableBytes();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    private void write(final String msg, final Channel channel) throws Exception {
        byte[] encryptedRespond = AES.encrypt(msg, kirin.clientKey);
        ByteBuf respond = Unpooled.wrappedBuffer(encryptedRespond);
        channel.writeAndFlush(respond);
    }
}