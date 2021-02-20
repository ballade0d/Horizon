package xyz.hstudio.horizon.configuration;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;

@LoadFrom("config.yml")
public class Config {

    @LoadPath("prefix")
    public static String PREFIX;

    @LoadPath("log")
    public static boolean LOG;

    @LoadPath("mysql.enabled")
    public static boolean MYSQL_ENABLED;
    @LoadPath("mysql.host")
    public static String MYSQL_HOST;
    @LoadPath("mysql.database")
    public static String MYSQL_DATABASE;
    @LoadPath("mysql.user")
    public static String MYSQL_USER;
    @LoadPath("mysql.password")
    public static String MYSQL_PASSWORD;
    @LoadPath("mysql.port")
    public static int MYSQL_PORT;

    @LoadPath("discord_integration.enabled")
    public static boolean DISCORD_INTEGRATION_ENABLED;
    @LoadPath("discord_integration.token")
    public static String DISCORD_INTEGRATION_TOKEN;
    @LoadPath("discord_integration.channel_name")
    public static String DISCORD_INTEGRATION_CHANNEL_NAME;

    @LoadPath("kirin.enabled")
    public static boolean KIRIN_ENABLED;
    @LoadPath("kirin.email")
    public static String KIRIN_EMAIL;
    @LoadPath("kirin.license")
    public static String KIRIN_LICENSE;

    @LoadPath("ghost_block_fix")
    public static boolean GHOST_BLOCK_FIX;
}