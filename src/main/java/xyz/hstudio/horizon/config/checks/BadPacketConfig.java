package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.config.Config;
import xyz.hstudio.horizon.config.annotation.Load;

public class BadPacketConfig extends Config {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    // TypeB
    @Load(file = "check.yml", path = "typeB.enabled")
    public boolean typeB_enabled = true;
}