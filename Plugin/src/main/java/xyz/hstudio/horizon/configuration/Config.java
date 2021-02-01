package xyz.hstudio.horizon.configuration;

import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.util.Yaml;

public class Config extends ConfigBase {

    private static final Yaml def = Yaml.loadConfiguration(Horizon.class.getResourceAsStream("/config.yml"));

    @LoadInfo(path = "prefix")
    public static String PREFIX;

    @LoadInfo(path = "mysql.enabled")
    public static boolean mysql_enabled;
    @LoadInfo(path = "mysql.host")
    public static String mysql_host;
    @LoadInfo(path = "mysql.database")
    public static String mysql_database;
    @LoadInfo(path = "mysql.user")
    public static String mysql_user;
    @LoadInfo(path = "mysql.password")
    public static String mysql_password;
    @LoadInfo(path = "mysql.port")
    public static int mysql_port;

    @LoadInfo(path = "discord_integration.enabled")
    public static boolean discord_integration_enabled;
    @LoadInfo(path = "discord_integration.token")
    public static String discord_integration_token;
    @LoadInfo(path = "discord_integration.channel_name")
    public static String discord_integration_channel_name;

    @LoadInfo(path = "kirin.enabled")
    public static boolean kirin_enabled;
    @LoadInfo(path = "kirin.license")
    public static String kirin_license;

    @LoadInfo(path = "ghost_block_fix")
    public static boolean ghost_block_fix;

    public static void load(Yaml yaml) {
        ConfigBase.load(Config.class, yaml, def);
    }
}