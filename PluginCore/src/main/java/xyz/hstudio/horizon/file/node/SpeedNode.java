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
    // NoSlow
    @Load(path = "noslow.enabled")
    public boolean noslow_enabled = true;
    @Load(path = "noslow.packet_vl")
    public int noslow_packet_vl = 8;
    @Load(path = "noslow.move_vl")
    public int noslow_move_vl = -1;
    @Load(path = "noslow.always_cancel")
    public boolean noslow_always_cancel = false;
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