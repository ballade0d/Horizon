package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class HitBoxNode extends CheckFile {

    // Reach
    @Load(path = "reach.enabled")
    public boolean reach_enabled = true;
    @Load(path = "reach.max_reach")
    public double reach_max_reach = 3.1;
    // Direction
    @Load(path = "direction.enabled")
    public boolean direction_enabled = true;
    @Load(path = "direction.box_expansion")
    public double direction_box_expansion = 0.2;

    public HitBoxNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}