package xyz.hstudio.horizon.wrapper;

import io.netty.channel.ChannelPipeline;
import lombok.Getter;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.util.enums.Version;

public abstract class AccessorBase {

    @Getter
    private static final AccessorBase inst = Version.VERSION.getAccessor();

    public abstract float sin(float v);

    public abstract float cos(float v);

    public abstract ChannelPipeline getPipeline(HPlayer p);
}