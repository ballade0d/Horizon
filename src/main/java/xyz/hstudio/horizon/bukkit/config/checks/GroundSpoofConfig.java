package xyz.hstudio.horizon.bukkit.config.checks;

import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;

public class GroundSpoofConfig extends Config {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
}