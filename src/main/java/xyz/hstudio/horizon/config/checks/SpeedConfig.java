package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class SpeedConfig extends CheckConfig<SpeedConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(file = "check.yml", path = "typeA.tolerance")
    public double typeA_tolerance = 0.08;
    // TypeB
    @Load(file = "check.yml", path = "typeB.enabled")
    public boolean typeB_enabled = true;

    @Override
    public SpeedConfig load() {
        return super.load(ModuleType.Speed.name().toLowerCase(), this);
    }
}