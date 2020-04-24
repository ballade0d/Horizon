package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class ESPNode extends CheckNode {

    public ESPNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}