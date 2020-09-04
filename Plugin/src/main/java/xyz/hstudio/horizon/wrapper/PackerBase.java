package xyz.hstudio.horizon.wrapper;

import lombok.Getter;
import xyz.hstudio.horizon.util.enums.Version;

public abstract class PackerBase {

    @Getter
    private static final PackerBase inst = Version.VERSION.getPacker();
}