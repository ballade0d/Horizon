package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class InventoryNode extends CheckFile {

    // TypeA
    @Load(path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(path = "typeA.checkRotation")
    public boolean typeA_checkRotation = true;
    @Load(path = "typeA.checkPosition")
    public boolean typeA_checkPosition = true;
    @Load(path = "typeA.checkAction")
    public boolean typeA_checkAction = true;
    @Load(path = "typeA.checkHit")
    public boolean typeA_checkHit = true;

    public InventoryNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}