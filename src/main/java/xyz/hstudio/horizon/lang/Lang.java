package xyz.hstudio.horizon.lang;

import com.google.common.primitives.Primitives;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.lang.annotation.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.lang.reflect.Field;

public class Lang {

    private final YamlLoader loader;

    @Load(path = "verbose")
    public String verbose;

    public Lang(final YamlLoader loader) {
        this.loader = loader;
        Lang.load(this);
    }

    /**
     * A nice and easy way to load values from configuration
     *
     * @throws IllegalStateException When config is miss
     * @author MrCraftGoo
     */
    private static void load(final Lang lang) {
        Field[] fields = lang.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Load annotation = field.getAnnotation(Load.class);
            if (annotation == null) {
                continue;
            }

            String path = annotation.path();

            YamlLoader loader = lang.loader;
            if (loader == null) {
                throw new IllegalStateException("Failed to load value: " + path + " [1]");
            }

            Object value = loader.get(path);

            if (value == null) {
                continue;
            }

            // Double.class.isAssignableFrom(double.class) is false :/
            if (!Primitives.unwrap(value.getClass()).isAssignableFrom(field.getType())) {
                throw new IllegalStateException("Failed to load value: " + path + " [2]");
            }

            try {
                lang.getClass().getFields()[i].set(lang, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Lang getLang(final String name) {
        return Horizon.getInst().langMap.getOrDefault(name, Horizon.getInst().langMap.get("original"));
    }
}