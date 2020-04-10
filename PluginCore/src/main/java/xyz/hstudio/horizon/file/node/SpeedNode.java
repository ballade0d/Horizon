package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class SpeedNode extends CheckFile {

    // Predict
    @Load(path = "predict.enabled")
    public boolean predict_enabled = true;
    @Load(path = "predict.tolerance")
    public double predict_tolerance = 0.08;
    @Load(path = "predict.cancel_type")
    public int predict_cancel_type = 2;
    @Load(path = "predict.only_noslow")
    public boolean predict_only_noslow = false;
    // Sprint
    @Load(path = "sprint.enabled")
    public boolean sprint_enabled = true;
    // Strafe
    @Load(path = "strafe.enabled")
    public boolean strafe_enabled = true;
    @Load(path = "strafe.threshold")
    public int strafe_threshold = 3;

    public SpeedNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}