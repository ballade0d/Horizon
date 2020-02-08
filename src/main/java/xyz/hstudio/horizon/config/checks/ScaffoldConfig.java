package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class ScaffoldConfig extends CheckConfig<ScaffoldConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    // TypeB
    @Load(file = "check.yml", path = "typeB.enabled")
    public boolean typeB_enabled = true;
    @Load(file = "check.yml", path = "typeB.max_angle")
    public double typeB_max_angle = 90;
    // TypeC
    @Load(file = "check.yml", path = "typeC.enabled")
    public boolean typeC_enabled = true;
    // TypeD
    @Load(file = "check.yml", path = "typeD.enabled")
    public boolean typeD_enabled = true;

    @Override
    public ScaffoldConfig load() {
        return super.load(ModuleType.Scaffold.name(), this);
    }
}