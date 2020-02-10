package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class HitBoxConfig extends CheckConfig<HitBoxConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(file = "check.yml", path = "typeA.max_reach")
    public double typeA_max_reach = 3.1;

    @Override
    public HitBoxConfig load() {
        return super.load(ModuleType.HitBox.name().toLowerCase(), this);
    }
}