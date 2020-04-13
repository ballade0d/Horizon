package xyz.hstudio.horizon.file.node;

import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.CheckFile;
import xyz.hstudio.horizon.file.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

public class KillAuraNode extends CheckFile {

    // Order
    @Load(path = "order.enabled")
    public boolean order_enabled = true;
    // SuperKb
    @Load(path = "superkb.enabled")
    public boolean superkb_enabled = true;
    // GCD
    @Load(path = "gcd.enabled")
    public boolean gcd_enabled = true;
    @Load(path = "gcd.strict")
    public boolean gcd_strict = false;
    // Direction
    @Load(path = "direction.enabled")
    public boolean direction_enabled = true;
    // InteractAutoBlock
    @Load(path = "interact_autoblock.enabled")
    public boolean interact_autoblock_enabled = true;
    // NormalAutoBlock
    @Load(path = "normal_autoblock.enabled")
    public boolean normal_autoblock_enabled = true;

    public KillAuraNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}