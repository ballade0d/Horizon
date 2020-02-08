package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class GroundSpoofConfig extends CheckConfig<GroundSpoofConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;

    @Override
    public GroundSpoofConfig load() {
        return super.load(ModuleType.GroundSpoof.name(), this);
    }
}