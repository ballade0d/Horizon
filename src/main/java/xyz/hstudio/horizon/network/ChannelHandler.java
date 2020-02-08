package xyz.hstudio.horizon.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.compat.PacketConverter;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.module.Module;

@RequiredArgsConstructor
public class ChannelHandler extends ChannelDuplexHandler {

    private static final String HANDLER_NAME = "horizon_packet_handler";
    private final HoriPlayer player;

    private static boolean runInbound(final HoriPlayer player, final Object packet) {
        Event event = PacketConverter.INSTANCE.convertIn(player, packet);
        if (event == null) {
            return true;
        }
        if (!event.pre()) {
            return false;
        }
        Module.doCheck(event, player);
        if (event.isCancelled()) {
            return false;
        }
        event.post();
        return true;
    }

    private static boolean runOutbound(final HoriPlayer player, final Object packet) {
        Event event = PacketConverter.INSTANCE.convertOut(player, packet);
        if (event == null) {
            return true;
        }
        if (!event.pre()) {
            return false;
        }
        Module.doCheck(event, player);
        if (event.isCancelled()) {
            return false;
        }
        event.post();
        return true;
    }

    public static void register(final HoriPlayer player, final ChannelPipeline pipeline) {
        ChannelDuplexHandler handler = new ChannelHandler(player);
        if (pipeline.get(HANDLER_NAME) != null) {
            pipeline.remove(HANDLER_NAME);
        }
        pipeline.addBefore("packet_handler", HANDLER_NAME, handler);
    }

    public static void unregister(final HoriPlayer player) {
        ChannelPipeline pipeline = player.pipeline;
        if (pipeline.get(HANDLER_NAME) != null) {
            pipeline.remove(HANDLER_NAME);
        }
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object packet) throws Exception {
        boolean pass = true;
        try {
            pass = ChannelHandler.runInbound(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (pass) {
                super.channelRead(ctx, packet);
            }
        }
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object packet, final ChannelPromise promise) throws Exception {
        boolean pass = true;
        try {
            pass = ChannelHandler.runOutbound(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (pass) {
                super.write(ctx, packet, promise);
            }
        }
    }
}