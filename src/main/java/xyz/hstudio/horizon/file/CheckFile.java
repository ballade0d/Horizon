package xyz.hstudio.horizon.file;

import org.apache.commons.lang.StringUtils;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckFile extends AbstractFile {

    // Cancel Violation
    @Load(path = "cancel_vl")
    public int cancel_vl = 1;
    @Load(path = "enabled")
    public boolean enabled = true;
    @Load(path = "enable_worlds")
    public List<String> enable_worlds = Collections.singletonList("*");
    @Load(path = "action")
    public Map<Integer, List<String>> action = new HashMap<>();

    @Override
    public Object getValue(final String path, final YamlLoader loader, final Class<?> type) {
        if (type != Map.class) {
            return loader.get(path);
        }

        Map<Integer, List<String>> map = new HashMap<>();
        for (String s : loader.getConfigurationSection(path).getKeys(false)) {
            if (!StringUtils.isNumeric(s)) {
                continue;
            }
            List<String> list = loader.isList(path + s) ?
                    loader.getStringList(path + "." + s) :
                    Collections.singletonList(loader.getString(path + "." + s));
            map.put(Integer.parseInt(s), list);
        }
        return map;
    }
}