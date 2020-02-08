package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class TimerConfig extends CheckConfig<TimerConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    @Load(file = "check.yml", path = "typeA.allow_ms")
    public int typeA_allow_ms = 50;

    @Override
    public TimerConfig load() {
        return super.load(ModuleType.Timer.name(), this);
    }
}