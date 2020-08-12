package xyz.hstudio.horizon.wrapper;

import lombok.Getter;
import xyz.hstudio.horizon.util.EnumVersion;

public abstract class AccessorBase {

    @Getter
    private static final AccessorBase inst = EnumVersion.VERSION.getAccessor();

    public abstract float sin(float v);

    public abstract float cos(float v);
}