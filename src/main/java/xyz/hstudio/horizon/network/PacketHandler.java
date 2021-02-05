package xyz.hstudio.horizon.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
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
        Event<?> event = PackerBase.received(p, packet);
        if (run(event, packet)) {
            super.channelRead(context, packet);
        }
    }

    @Override
    public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {
        Event<?> event = PackerBase.sent(p, packet);
        if (run(event, packet)) {
            super.write(context, packet, promise);
        }
    }

    private boolean run(Event<?> event, Object packet) {
        try {
            if (event == null) {
                return true;
            }
            if (!event.pre()) {
                return false;
            } else {
                for (CheckBase check : p.checks.values()) {
                    check.run(event);
                }
                event.post();
                event.apply(packet);
                return !event.isCancelled();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return true;
        }
    }

    public void unregister() {
        if (p.pipeline.get(HANDLER_NAME) != null) p.pipeline.remove(HANDLER_NAME);
    }
}