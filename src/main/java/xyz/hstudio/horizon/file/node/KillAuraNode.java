package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class KillAuraNode extends CheckFile {

    // TypeA
    @Load(path = "typeA.enabled")
    public boolean typeA_enabled = true;
    // TypeB
    @Load(path = "typeB.enabled")
    public boolean typeB_enabled = true;
    // TypeC
    @Load(path = "typeC.enabled")
    public boolean typeC_enabled = true;
    // TypeD
    @Load(path = "typeD.enabled")
    public boolean typeD_enabled = true;
    // TypeE
    @Load(path = "typeE.enabled")
    public boolean typeE_enabled = true;
    @Load(path = "typeE.cancel_type")
    public int typeE_cancel_type = 2;
    // TypeF
    @Load(path = "typeF.enabled")
    public boolean typeF_enabled = true;
    @Load(path = "typeF.cancel_type")
    public int typeF_cancel_type = 2;

    public KillAuraNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}