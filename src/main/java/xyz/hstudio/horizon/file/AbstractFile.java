package xyz.hstudio.horizon.file;

import com.google.common.primitives.Primitives;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.lang.reflect.Field;

public abstract class AbstractFile {

    public abstract Object getValue(final String path, final YamlLoader loader, final Class<?> type);

    public static <T extends AbstractFile> T load(final String pathPrefix, final T instance, final YamlLoader loader) {
        Field[] fields = instance.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Load annotation = field.getAnnotation(Load.class);
            if (annotation == null) {
                continue;
            }

            String path = pathPrefix == null ? annotation.path() : pathPrefix + "." + annotation.path();

            Object value = instance.getValue(path, loader, field.getType());

            // Double.class.isAssignableFrom(double.class) is false :/
            if (!Primitives.unwrap(value.getClass()).isAssignableFrom(field.getType())) {
                throw new IllegalStateException("Failed to load value: " + path);
            }

            try {
                instance.getClass().getFields()[i].set(instance, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
}