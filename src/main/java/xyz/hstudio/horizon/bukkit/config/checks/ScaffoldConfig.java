package xyz.hstudio.horizon.bukkit.config.checks;

import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;

public class ScaffoldConfig extends Config {

    @Load(file = "check.yml", path = "typeB.max_angle")
    public double typeB_max_angle = 90;
}