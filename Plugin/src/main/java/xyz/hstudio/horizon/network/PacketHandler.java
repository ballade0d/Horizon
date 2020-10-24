package xyz.hstudio.horizon.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.OutEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.wrapper.PackerBase;

public class PacketHandler extends ChannelDuplexHandler {

    private static final String HANDLER_NAME = "horizon_client";

    private final HPlayer p;

    public PacketHandler(HPlayer p) {
        this.p = p;
        p.pipeline.addBefore("packet_handler", HANDLER_NAME, this);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object packet) throws Exception {
        boolean cancelled = false;
        try {
            InEvent event = PackerBase.getInst().received(p, packet);
            if (event != null) {
                if (!event.pre()) {
                    cancelled = true;
                } else {
                    for (CheckBase check : p.getChecks()) {
                        check.received(event);
                    }
                    event.post();
                    cancelled = event.isCancelled();
                }
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
        boolean cancelled = false;
        try {
            OutEvent event = PackerBase.getInst().sent(p, packet);
            if (event != null) {
                if (!event.pre()) {
                    cancelled = true;
                } else {
                    for (CheckBase check : p.getChecks()) {
                        check.sent(event);
                    }
                    event.post();
                    cancelled = event.isCancelled();
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (!cancelled) {
                super.write(context, packet, promise);
            }
        }
    }

    public void unregister() {
        if (p.pipeline.get(HANDLER_NAME) != null) {
            p.pipeline.remove(HANDLER_NAME);
        }
    }
}