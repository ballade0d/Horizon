package xyz.hstudio.horizon.kirin;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.base64.Base64;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.configuration.Config;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class Kirin extends ChannelInboundHandlerAdapter {

    private PublicKey key;
    private boolean infoSent;

    public void verify() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                channel.pipeline().addLast(Kirin.this);
            }
        });
        ChannelFuture future = bootstrap.connect("127.0.0.1", 6666);
        try {
            future.sync();
            synchronized (this) {
                this.wait();
            }
            group.shutdownGracefully();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = Base64.decode((ByteBuf) msg);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        if (infoSent) {
            Cipher decrypt = Cipher.getInstance("RSA");
            decrypt.init(Cipher.DECRYPT_MODE, key);
            ByteBuf decrypted = Unpooled.buffer().writeBytes(decrypt.doFinal(bytes));
            boolean available = decrypted.readBoolean();
            int size = decrypted.readInt();
            for (int i = 0; i < size; i++) {
                int length = decrypted.readInt();
                byte[] strBytes = new byte[length];
                decrypted.readBytes(strBytes);
                String content = new String(strBytes);
                Logger.msg("KIRIN", content);
            }

            if (available) {
                // Startup here
            }

            synchronized (this) {
                this.notify();
            }
            return;
        }
        this.key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] license = (Config.KIRIN_EMAIL + "|" + Config.KIRIN_LICENSE).getBytes(StandardCharsets.UTF_8);

        ByteBuf encrypted = Unpooled.buffer().writeBytes(cipher.doFinal(license));
        ctx.writeAndFlush(Base64.encode(encrypted));
        this.infoSent = true;
    }
}