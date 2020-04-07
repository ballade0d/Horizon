package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class SpeedNode extends CheckFile {

    // TypeA
    @Load(path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(path = "typeA.tolerance")
    public double typeA_tolerance = 0.08;
    @Load(path = "typeA.cancel_type")
    public int typeA_cancel_type = 2;
    @Load(path = "typeA.only_noslow")
    public boolean typeA_only_noslow = false;
    // TypeB
    @Load(path = "typeB.enabled")
    public boolean typeB_enabled = true;
    // TypeC
    @Load(path = "typeC.enabled")
    public boolean typeC_enabled = true;
    @Load(path = "typeC.threshold")
    public int typeC_threshold = 3;

    public SpeedNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}