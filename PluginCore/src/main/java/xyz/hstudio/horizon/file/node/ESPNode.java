package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class ESPNode extends CheckNode {

    @Load(path = "update_interval")
    public int update_interval = 2;
    @Load(path = "check_angle")
    public boolean check_angle = false;
    @Load(path = "max_distance")
    public double max_distance = 80.0;

    public ESPNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}