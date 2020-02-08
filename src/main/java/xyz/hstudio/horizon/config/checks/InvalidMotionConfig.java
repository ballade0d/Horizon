package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class InvalidMotionConfig extends CheckConfig<InvalidMotionConfig> {

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

    @Override
    public InvalidMotionConfig load() {
        return super.load(ModuleType.InvalidMotion.name(), this);
    }
}