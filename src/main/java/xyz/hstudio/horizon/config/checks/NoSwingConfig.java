package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class NoSwingConfig extends CheckConfig<NoSwingConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;

    @Override
    public NoSwingConfig load() {
        return super.load(ModuleType.NoSwing.name().toLowerCase(), this);
    }
}