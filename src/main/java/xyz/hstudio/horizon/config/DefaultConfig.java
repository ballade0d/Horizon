package xyz.hstudio.horizon.config;

import xyz.hstudio.horizon.config.annotation.Load;

import java.util.Arrays;
import java.util.List;

public class DefaultConfig extends AbstractConfig {

    @Load(file = "config.yml", path = "prefix")
    public String prefix = "§9§lHorizon §1§l>> §r§3";

    @Load(file = "config.yml", path = "log")
    public boolean log = true;

    @Load(file = "config.yml", path = "command_alias")
    public List<String> command_alias = Arrays.asList("hz", "hori");

    @Load(file = "config.yml", path = "personalized_themes.enabled")
    public boolean personalized_themes_enabled = true;
    @Load(file = "config.yml", path = "personalized_themes.default_lang")
    public String personalized_themes_default_lang = "en_US";

    @Load(file = "config.yml", path = "kirin.enabled")
    public boolean kirin_enabled = false;
    @Load(file = "config.yml", path = "kirin.licence")
    public String kirin_licence = "";

    public DefaultConfig load() {
        return super.load(null, this);
    }
}