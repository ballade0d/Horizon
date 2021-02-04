package xyz.hstudio.horizon.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.util.Yaml;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@LoadFrom("config.yml")
public class Config {

    @LoadInfo("prefix")
    public static String PREFIX;

    @LoadInfo("mysql.enabled")
    public static boolean MYSQL_ENABLED;
    @LoadInfo("mysql.host")
    public static String MYSQL_HOST;
    @LoadInfo("mysql.database")
    public static String MYSQL_DATABASE;
    @LoadInfo("mysql.user")
    public static String MYSQL_USER;
    @LoadInfo("mysql.password")
    public static String MYSQL_PASSWORD;
    @LoadInfo("mysql.port")
    public static int MYSQL_PORT;

    @LoadInfo("discord_integration.enabled")
    public static boolean DISCORD_INTEGRATION_ENABLED;
    @LoadInfo("discord_integration.token")
    public static String DISCORD_INTEGRATION_TOKEN;
    @LoadInfo("discord_integration.channel_name")
    public static String DISCORD_INTEGRATION_CHANNEL_NAME;

    @LoadInfo("kirin.enabled")
    public static boolean KIRIN_ENABLED;
    @LoadInfo("kirin.license")
    public static String KIRIN_LICENSE;

    @LoadInfo("ghost_block_fix")
    public static boolean GHOST_BLOCK_FIX;

    public static void load(Class<?> clazz) {
        LoadFrom definer = clazz.getAnnotation(LoadFrom.class);
        if (definer == null) {
            throw new IllegalStateException();
        }

        Horizon inst = JavaPlugin.getPlugin(Horizon.class);
        YamlConfiguration def = Yaml.loadConfiguration(inst.getResource(definer.value()));

        File file = new File(inst.getDataFolder(), definer.value());
        if (!file.isFile()) {
            inst.saveResource(definer.value(), true);
        }

        YamlConfiguration yaml = Yaml.loadConfiguration(file);

        for (Field field : clazz.getDeclaredFields()) {
            LoadInfo annotation = field.getAnnotation(LoadInfo.class);
            if (annotation == null || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);

            String path = annotation.value();

            try {
                if (!yaml.contains(path)) {
                    throw new IllegalStateException("Cannot find the value in the config.");
                }
                field.set(null, yaml.get(path));
            } catch (Exception e) {
                Logger.msg("WARN", "Failed to load the value " + path + " in the file " + definer.value() + " ! Using default value. Reason: " + e.getMessage());
                try {
                    field.set(null, def.get(path));
                } catch (Exception ignore) {
                }
            }

            field.setAccessible(false);
        }
    }
}