package xyz.hstudio.horizon.wrapper;

import lombok.Getter;
import xyz.hstudio.horizon.util.EnumVersion;

public abstract class PackerBase {

    @Getter
    private static final PackerBase inst = EnumVersion.VERSION.getPacker();
}