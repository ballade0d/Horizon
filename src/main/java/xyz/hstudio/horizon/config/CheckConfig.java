package xyz.hstudio.horizon.config;

import xyz.hstudio.horizon.config.annotation.Load;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CheckConfig<T extends CheckConfig<T>> extends AbstractConfig {

    // Cancel Violation
    @Load(file = "check.yml", path = "cancel_vl")
    public int cancel_vl = 1;
    @Load(file = "check.yml", path = "enabled")
    public boolean enabled = true;
    @Load(file = "check.yml", path = "debug")
    public boolean debug = true;
    @Load(file = "check.yml", path = "action")
    public Map<Integer, List<String>> action = new HashMap<>();

    public abstract T load();
}