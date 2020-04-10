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
    public boolean gcd_strict = true;
    // Direction
    @Load(path = "direction.enabled")
    public boolean direction_enabled = true;
    // InteractAutoBlock
    @Load(path = "interact_autoblock.enabled")
    public boolean interact_autoblock_enabled = true;
    @Load(path = "interact_autoblock.cancel_type")
    public int interact_autoblock_cancel_type = 2;
    // NormalAutoBlock
    @Load(path = "normal_autoblock.enabled")
    public boolean normal_autoblock_enabled = true;
    @Load(path = "normal_autoblock.cancel_type")
    public int normal_autoblock_cancel_type = 2;

    public KillAuraNode load(final String pathPrefix, final YamlLoader loader) {
        return AbstractFile.load(pathPrefix, this, loader);
    }
}