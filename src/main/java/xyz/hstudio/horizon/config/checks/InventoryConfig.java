package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.config.Config;
import xyz.hstudio.horizon.config.annotation.Load;

public class InventoryConfig extends Config {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(file = "check.yml", path = "typeA.checkRotation")
    public boolean typeA_checkRotation = true;
    @Load(file = "check.yml", path = "typeA.checkPosition")
    public boolean typeA_checkPosition = true;
    @Load(file = "check.yml", path = "typeA.checkAction")
    public boolean typeA_checkAction = true;
    @Load(file = "check.yml", path = "typeA.checkHit")
    public boolean typeA_checkHit = true;
}