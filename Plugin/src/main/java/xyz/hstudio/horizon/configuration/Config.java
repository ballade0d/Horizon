package xyz.hstudio.horizon.configuration;

import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.util.Yaml;

public class Config extends ConfigBase {

    private static final Yaml def = Yaml.loadConfiguration(Horizon.class.getResourceAsStream("/config.yml"));

    @LoadInfo(path = "prefix")
    public static String PREFIX;

    public static void load(Yaml yaml) {
        ConfigBase.load(Config.class, yaml, def);
    }
}