package xyz.hstudio.horizon.configuration;

import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.util.Yaml;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class ConfigBase {

    public static void load(Class<? extends ConfigBase> clazz, Yaml yaml, Yaml def) {
        for (Field field : clazz.getFields()) {
            LoadInfo annotation = field.getAnnotation(LoadInfo.class);
            if (annotation == null || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            String path = annotation.path();

            Object value = yaml.get(path);

            try {
                field.set(null, value);
            } catch (Exception ignore) {
                Logger.msg("WARN", "Failed to load the value " + path + " in the config! Using default value.");
                value = def.get(path);
                try {
                    field.set(null, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
