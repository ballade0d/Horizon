package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class AntiVelocityNode extends CheckNode {

    // TypeA
    @Load(path = "typeA.enabled")
    public boolean typeA_enabled = true;

    public AntiVelocityNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}