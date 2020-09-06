package xyz.hstudio.horizon.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.module.check.CheckBase;
import xyz.hstudio.horizon.wrapper.PackerBase;

public class PacketHandler extends ChannelDuplexHandler {

    private static final String HANDLER_NAME = "horizon_client";

    private final HPlayer p;

    public PacketHandler(HPlayer p) {
        this.p = p;
        p.getPipeline().addBefore("packet_handler", HANDLER_NAME, this);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        boolean cancelled = false;
        try {
            InEvent event = PackerBase.getInst().received(p, packet);
            if (event != null) {
                for (CheckBase check : p.getChecks()) {
                    check.received(event);
                }
                cancelled = event.isCancelled();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (!cancelled) {
                super.channelRead(context, packet);
            }
        }
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        super.write(context, packet, promise);
    }

    public void unregister() {
        p.getPipeline().remove(HANDLER_NAME);
    }
}