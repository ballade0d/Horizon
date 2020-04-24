package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class InvalidMotionNode extends CheckNode {

    @Load(path = "allowed_wall_jump")
    public int allowed_client_blocks = 2;
    // TypeA
    @Load(path = "predict.enabled")
    public boolean predict_enabled = true;
    @Load(path = "predict.tolerance")
    public double predict_tolerance = 0.001;
    // TypeB
    @Load(path = "step.enabled")
    public boolean step_enabled = true;
    // TypeC
    @Load(path = "packet.enabled")
    public boolean packet_enabled = true;

    public InvalidMotionNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}