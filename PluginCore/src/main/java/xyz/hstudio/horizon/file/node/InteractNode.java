package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class InteractNode extends CheckNode {

    // TypeA
    @Load(path = "packet.enabled")
    public boolean packet_enabled = true;
    // TypeB
    @Load(path = "angle.enabled")
    public boolean angle_enabled = true;
    @Load(path = "angle.max_angle")
    public double angle_max_angle = 90;
    // TypeC
    @Load(path = "order.enabled")
    public boolean order_enabled = true;
    // TypeD
    @Load(path = "direction.enabled")
    public boolean direction_enabled = true;

    public InteractNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}