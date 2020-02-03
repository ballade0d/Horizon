package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.config.Config;
import xyz.hstudio.horizon.config.annotation.Load;

public class HitBoxConfig extends Config {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(file = "check.yml", path = "typeA.max_reach")
    public double typeA_max_reach = 3.1;
}