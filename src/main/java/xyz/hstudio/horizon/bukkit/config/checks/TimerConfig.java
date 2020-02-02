package xyz.hstudio.horizon.bukkit.config.checks;

import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;

public class TimerConfig extends Config {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(file = "check.yml", path = "typeA.allow_ms")
    public int typeA_allow_ms = 50;
}