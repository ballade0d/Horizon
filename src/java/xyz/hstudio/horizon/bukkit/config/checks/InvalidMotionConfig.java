package xyz.hstudio.horizon.bukkit.config.checks;

import xyz.hstudio.horizon.bukkit.config.Config;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;

public class InvalidMotionConfig extends Config {

    @Load(file = "check.yml", path = "tolerance")
    public double tolerance = 0.001;
}