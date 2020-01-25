package xyz.hstudio.horizon.bukkit.config.checks;

import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;

public class KillAuraConfig extends Config {

    // TypeF
    @Load(file = "check.yml", path = "typeF.max_angle")
    public double typeF_max_angle = 0.6;
}