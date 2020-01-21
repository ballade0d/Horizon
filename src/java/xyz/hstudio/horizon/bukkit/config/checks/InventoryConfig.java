package xyz.hstudio.horizon.bukkit.config.checks;

import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;

public class InventoryConfig extends Config {

    // TypeA
    @Load(file = "check.yml", path = "typeA.checkRotation")
    public boolean typeA_checkRotation = true;
    @Load(file = "check.yml", path = "typeA.checkAction")
    public boolean typeA_checkAction = true;
    @Load(file = "check.yml", path = "typeA.checkHit")
    public boolean typeA_checkHit = true;
}