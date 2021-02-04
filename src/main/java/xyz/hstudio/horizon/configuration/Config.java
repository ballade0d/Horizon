package xyz.hstudio.horizon.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.util.Yaml;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@LoadClass(file = "/config.yml")
public class Config {

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

    public static void load(Class<?> clazz) {
        LoadClass definer = clazz.getAnnotation(LoadClass.class);
        if (definer == null) {
            throw new IllegalStateException();
        }

        Horizon inst = JavaPlugin.getPlugin(Horizon.class);
        YamlConfiguration def = Yaml.loadConfiguration(Horizon.class.getResourceAsStream(definer.file()));

        File file = new File(inst.getDataFolder(), definer.file());
        if (!file.isFile()) {
            inst.saveResource(definer.file(), true);
        }

        YamlConfiguration yaml = Yaml.loadConfiguration(file);

        for (Field field : clazz.getDeclaredFields()) {
            LoadInfo annotation = field.getAnnotation(LoadInfo.class);
            if (annotation == null || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);

            String path = annotation.path();

            try {
                if (!yaml.contains(path)) {
                    throw new Exception();
                }
                field.set(null, yaml.get(path));
            } catch (Exception e) {
                Logger.msg("WARN", "Failed to load the value " + path + " in the config! Using default value. Stacktrace:");
                e.printStackTrace();
                try {
                    field.set(null, def.get(path));
                } catch (Exception ignore) {
                }
            }

            field.setAccessible(false);
        }
    }
}