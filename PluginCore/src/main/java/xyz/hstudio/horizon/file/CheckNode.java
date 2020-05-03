package xyz.hstudio.horizon.file;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.util.*;

public class CheckNode extends AbstractFile {

    // Cancel Violation
    @Load(path = "cancel_vl")
    public int cancel_vl = 1;
    @Load(path = "enabled")
    public boolean enabled = true;
    @Load(path = "disable_worlds")
    public Set<String> disable_worlds = Collections.emptySet();
    @Load(path = "action")
    public Map<Integer, List<String>> action = new HashMap<>();

    @Override
    public Object getValue(final String path, final YamlLoader loader, final Class<?> type) {
        if (type != Map.class) {
            return loader.get(path);
        }

        Map<Integer, List<String>> map = new LinkedHashMap<>();
        if (loader.get(path) == null) {
            return map;
        }
        List<Integer> vls = new ArrayList<>();
        for (String s : loader.getConfigurationSection(path).getKeys(false)) {
            if (!StringUtils.isNumeric(s)) {
                continue;
            }
            vls.add(Integer.parseInt(s));
        }
        Collections.sort(vls);
        for (int i = vls.size() - 1; i >= 0; i--) {
            int vl = vls.get(i);
            List<String> list = new ArrayList<>(loader.isList(path + "." + vl) ?
                    loader.getStringList(path + "." + vl) :
                    Collections.singletonList(loader.getString(path + "." + vl)));
            for (int line = 0; line < list.size(); line++) {
                list.set(line, ChatColor.translateAlternateColorCodes('&', list.get(line)));
            }
            map.put(vl, list);
        }
        return map;
    }
}