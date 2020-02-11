package xyz.hstudio.horizon.config;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.google.common.primitives.Primitives;
import org.apache.commons.lang.StringUtils;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.config.annotation.Load;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfig {

    /**
     * A nice and easy way to load values from configuration
     *
     * @return CheckConfig instance
     * @throws IllegalStateException When config is miss
     * @author MrCraftGoo
     */
    public <T extends AbstractConfig> T load(final String pathPrefix, final T conf) {
        FieldAccess access = FieldAccess.get(conf.getClass());
        Field[] fields = access.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Load annotation = field.getAnnotation(Load.class);
            if (annotation == null) {
                continue;
            }

            String file = annotation.file();
            String path = pathPrefix == null ? annotation.path() : pathPrefix + "." + annotation.path();

            YamlLoader config = Horizon.getInst().configMap.get(file);
            if (config == null) {
                throw new IllegalStateException("Failed to load value: " + path + " [1]");
            }

            Object value = config.get(path);

            if (field.getType() == Map.class) {
                if (value == null) {
                    config.set(path, value = new HashMap<>());
                } else {
                    Map<Integer, List<String>> map = new HashMap<>();
                    for (String s : config.getConfigurationSection(path).getKeys(false)) {
                        if (!StringUtils.isNumeric(s)) {
                            continue;
                        }
                        List<String> list = config.isList(path + s) ?
                                config.getStringList(path + "." + s) :
                                Collections.singletonList(config.getString(path + "." + s));
                        map.put(Integer.parseInt(s), list);
                    }
                    value = map;
                }
            } else {
                if (value == null) {
                    config.set(path, value = access.get(conf, i));
                }

                // Double.class.isAssignableFrom(double.class) is false :/
                if (!Primitives.unwrap(value.getClass()).isAssignableFrom(field.getType())) {
                    throw new IllegalStateException("Failed to load value: " + path + " [2]");
                }
            }

            access.set(conf, i, value);
        }

        return conf;
    }
}