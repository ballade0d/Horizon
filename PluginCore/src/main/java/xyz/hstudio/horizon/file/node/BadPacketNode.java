package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckNode;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class BadPacketNode extends CheckNode {

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
    @Load(path = "typeE.enabled")
    public boolean typeE_enabled = true;
    @Load(path = "typeF.enabled")
    public boolean typeF_enabled = true;

    public BadPacketNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}