package xyz.hstudio.horizon.configuration;

import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.util.Yaml;

public class Config extends ConfigBase {

    private static final Yaml def = Yaml.loadConfiguration(Horizon.class.getResourceAsStream("/config.yml"));

    @LoadInfo(path = "prefix")
    public static String PREFIX;
    @LoadInfo(path = "mysql.enabled")
    public static boolean mysql_enabled = false;
    @LoadInfo(path = "mysql.host")
    public static String mysql_host = "";
    @LoadInfo(path = "mysql.database")
    public static String mysql_database = "";
    @LoadInfo(path = "mysql.user")
    public static String mysql_user = "";
    @LoadInfo(path = "mysql.password")
    public static String mysql_password = "";
    @LoadInfo(path = "mysql.port")
    public static int mysql_port = 3306;
    @LoadInfo(path = "discord_integration.enabled")
    public boolean discord_integration_enabled = false;
    @LoadInfo(path = "discord_integration.webhookURL")
    public String discord_integration_webhookURL = "";
    @LoadInfo(path = "kirin.enabled")
    public boolean kirin_enabled = true;
    @LoadInfo(path = "kirin.license")
    public String kirin_license = "";


    public static void load(Yaml yaml) {
        ConfigBase.load(Config.class, yaml, def);
    }
}