package xyz.hstudio.horizon.bukkit.config.checks;

import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;

public class HitBoxConfig extends Config {

    @Load(file = "check.yml", path = "typeA.max_reach")
    public double typeA_max_reach = 3.1;
}