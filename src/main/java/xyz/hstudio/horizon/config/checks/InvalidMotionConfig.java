package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.config.Config;
import xyz.hstudio.horizon.config.annotation.Load;

public class InvalidMotionConfig extends Config {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(file = "check.yml", path = "typeA.tolerance")
    public double typeA_tolerance = 0.001;
    // TypeB
    @Load(file = "check.yml", path = "typeB.enabled")
    public boolean typeB_enabled = true;
    // TypeC
    @Load(file = "check.yml", path = "typeC.enabled")
    public boolean typeC_enabled = true;
    @Load(file = "check.yml", path = "typeC.tolerance")
    public double typeC_tolerance = -0.02;
}