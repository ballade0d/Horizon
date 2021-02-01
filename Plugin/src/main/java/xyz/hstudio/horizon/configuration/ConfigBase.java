package xyz.hstudio.horizon.configuration;

import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.util.Yaml;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class ConfigBase {

    public static void load(Class<? extends ConfigBase> clazz, Yaml yaml, Yaml def) {
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
