package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class InvalidMotionNode extends CheckFile {

    // TypeA
    @Load(path = "predict.enabled")
    public boolean predict_enabled = true;
    @Load(path = "predict.tolerance")
    public double predict_tolerance = 0.001;
    @Load(path = "predict.wall_jump")
    public boolean predict_wall_jump = true;
    // TypeB
    @Load(path = "step.enabled")
    public boolean step_enabled = true;
    // TypeC
    @Load(path = "fastfall.enabled")
    public boolean fastfall_enabled = true;
    @Load(path = "fastfall.tolerance")
    public double fastfall_tolerance = -0.02;

    public InvalidMotionNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}