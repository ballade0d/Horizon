package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class AntiVelocityConfig extends CheckConfig<AntiVelocityConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;

    @Override
    public AntiVelocityConfig load() {
        return super.load(ModuleType.AntiVelocity.name().toLowerCase(), this);
    }
}