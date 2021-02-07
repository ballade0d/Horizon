package xyz.hstudio.horizon.configuration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.util.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    @LoadInfo("kirin.email")
    public static String KIRIN_EMAIL;
    @LoadInfo("kirin.license")
    public static String KIRIN_LICENSE;

    @LoadInfo("ghost_block_fix")
    public static boolean GHOST_BLOCK_FIX;

    private static final Map<File, Class<?>> listener = new ConcurrentHashMap<>();

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

        listener.put(file, clazz);

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
                Logger.msg("WARN", "Failed to load the value " + path + " in the file " + definer.value() + "! Using default value. Reason: " + e.getMessage());
                try {
                    field.set(null, def.get(path));
                } catch (Exception ignore) {
                }
            }

            field.setAccessible(false);
        }
    }

    public static void watcher() throws IOException {
        // Use Set to deduplication so every directory will have only 1 listener
        Set<Path> parents = new HashSet<>();
        for (Map.Entry<File, Class<?>> entry : listener.entrySet()) {
            Path parent = entry.getKey().toPath().getParent();
            parents.add(parent);
        }
        ExecutorService threadPool = Executors.newFixedThreadPool(parents.size(),
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("Horizon Auto-Reloading Thread")
                        .build());
        for (Path parent : parents) {
            WatchService service = FileSystems.getDefault().newWatchService();
            // Register the service for the path
            parent.register(service, StandardWatchEventKinds.ENTRY_MODIFY);
            File dir = parent.toFile();
            threadPool.execute(() -> {
                while (true) {
                    WatchKey key;
                    try {
                        key = service.take();
                    } catch (InterruptedException e) {
                        Logger.msg("WARN", "An error occurred when executing WatchService of directory " + parent);
                        break;
                    }

                    // Use Set to deduplication
                    Set<String> names = new HashSet<>();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        names.add(event.context().toString());
                    }
                    for (String name : names) {
                        File changed = new File(dir, name);
                        Class<?> clazz = listener.get(changed);
                        if (clazz == null || changed.length() == 0) {
                            continue;
                        }
                        try {
                            // Test the file first, if an error occurred, skip
                            new YamlConfiguration().load(new InputStreamReader(new FileInputStream(changed), StandardCharsets.UTF_8));

                            // Finally, reload the configuration
                            load(clazz);
                        } catch (Exception ignore) {
                        }
                    }
                    key.reset();
                }
            });
        }
    }
}