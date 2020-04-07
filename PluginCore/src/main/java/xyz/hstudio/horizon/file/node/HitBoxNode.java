package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class HitBoxNode extends CheckFile {

    // TypeA
    @Load(path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(path = "typeA.max_reach")
    public double typeA_max_reach = 3.1;

    public HitBoxNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}