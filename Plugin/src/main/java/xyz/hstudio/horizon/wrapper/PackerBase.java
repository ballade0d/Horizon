package xyz.hstudio.horizon.wrapper;

import lombok.Getter;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Version;

public abstract class PackerBase {

    protected static final Vector3D INVALID_7 = new Vector3D(-1, 255, -1);
    protected static final Vector3D INVALID_8 = new Vector3D(-1, -1, -1);

    @Getter
    private static final PackerBase inst = Version.VERSION.getPacker();

    public abstract InEvent received(HPlayer p, Object packet);
}