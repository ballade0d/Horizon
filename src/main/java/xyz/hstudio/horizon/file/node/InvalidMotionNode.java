package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class InvalidMotionNode extends CheckFile {

    // TypeA
    @Load(path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(path = "typeA.tolerance")
    public double typeA_tolerance = 0.001;
    @Load(path = "typeA.wall_jump")
    public boolean typeA_wall_jump = true;
    // TypeB
    @Load(path = "typeB.enabled")
    public boolean typeB_enabled = true;
    // TypeC
    @Load(path = "typeC.enabled")
    public boolean typeC_enabled = true;
    @Load(path = "typeC.tolerance")
    public double typeC_tolerance = -0.02;

    public InvalidMotionNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}