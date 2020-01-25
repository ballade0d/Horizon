package xyz.hstudio.horizon.bukkit.config;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.google.common.primitives.Primitives;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.annotation.Load;
import xyz.hstudio.horizon.bukkit.util.YamlLoader;

import java.lang.reflect.Field;

public abstract class Config {

    @Load(file = "check.yml", path = "enabled")
    public boolean enabled = true;
    @Load(file = "check.yml", path = "debug")
    public boolean debug = true;

    /**
     * A nice and easy way to load values from configuration
     *
     * @return Config instance
     * @throws IllegalStateException When config is miss
     * @author MrCraftGoo
     */
    public static <T extends Config> T load(final ModuleType type, final T conf) throws IllegalStateException {
        FieldAccess access = FieldAccess.get(conf.getClass());
        Field[] fields = access.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Load annotation = field.getAnnotation(Load.class);
            if (annotation == null) {
                continue;
            }

            String file = annotation.file();
            String path = type.name().toLowerCase() + "." + annotation.path();

            YamlLoader config = Horizon.getInst().configMap.get(file);
            if (config == null) {
                throw new IllegalStateException("Failed to load value: " + path + " [1]");
            }

            Object value = config.get(path);
            if (value == null) {
                config.set(path, value = access.get(conf, i));
            }

            // Double.class.isAssignableFrom(double.class) is false :/
            if (!Primitives.unwrap(value.getClass()).isAssignableFrom(access.getFieldTypes()[i])) {
                throw new IllegalStateException("Failed to load value: " + path + " [2]");
            }

            access.set(conf, i, value);
        }

        return conf;
    }
}