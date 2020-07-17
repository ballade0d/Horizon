package xyz.hstudio.horizon.file;

import com.google.common.primitives.Primitives;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractFile {

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

            if (value == null) {
                continue;
            }

            if (field.getType() == Set.class && value.getClass() == ArrayList.class) {
                value = new HashSet<>((List<String>) value);
            }

            // Double.class.isAssignableFrom(double.class) is false :/
            if (!field.getType().isAssignableFrom(Primitives.unwrap(value.getClass()))) {
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

    public abstract Object getValue(String path, YamlLoader loader, Class<?> type);
}