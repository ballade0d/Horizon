package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class ScaffoldNode extends CheckNode {

    // TypeA
    @Load(path = "order.enabled")
    public boolean order_enabled = true;
    // TypeB
    @Load(path = "direction.enabled")
    public boolean direction_enabled = true;

    public ScaffoldNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}