package xyz.hstudio.horizon.config;

import xyz.hstudio.horizon.config.annotation.Load;

public class DefaultConfig extends AbstractConfig {

    @Load(file = "config.yml", path = "prefix")
    public String prefix = "§9§lHorizon §1§l>> §r§3";

    @Load(file = "config.yml", path = "kirin.enabled")
    public boolean kirin_enabled = false;
    @Load(file = "config.yml", path = "kirin.licence")
    public String kirin_licence = "";

    public DefaultConfig load() {
        return super.load(null, this);
    }
}