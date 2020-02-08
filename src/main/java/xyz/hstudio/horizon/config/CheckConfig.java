package xyz.hstudio.horizon.config;

import xyz.hstudio.horizon.config.annotation.Load;

public abstract class CheckConfig<T extends CheckConfig<T>> extends AbstractConfig {

    // Cancel Violation
    @Load(file = "check.yml", path = "cancel_vl")
    public int cancel_vl = 1;
    @Load(file = "check.yml", path = "enabled")
    public boolean enabled = true;
    @Load(file = "check.yml", path = "debug")
    public boolean debug = true;

    public abstract T load();
}