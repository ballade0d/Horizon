package xyz.hstudio.horizon.configuration;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.util.Yaml;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Execution {

    private static final Map<Detection, TIntObjectMap<List<String>>> collection = new EnumMap<>(Detection.class);
    private static final TIntObjectMap<List<String>> EMPTY = new TIntObjectHashMap<>(0);

    public static TIntObjectMap<List<String>> getActionMap(Detection detection) {
        return collection.getOrDefault(detection, EMPTY);
    }

    public static void load(Yaml yaml) {
        for (Detection detection : Detection.values()) {
            String name = detection.name();
            if (!yaml.isConfigurationSection(name)) {
                continue;
            }
            TIntObjectMap<List<String>> action = new TIntObjectHashMap<>();
            for (String vl : yaml.getConfigurationSection(name).getKeys(false)) {
                try {
                    action.put(Integer.parseInt(vl), yaml.getStringList(name + "." + vl));
                } catch (NumberFormatException e) {
                    Logger.msg("WARN", "Failed to load the execution of " + name + " ! Reason: '" + vl + "' is not a integer.");
                }
            }
            collection.put(detection, action);
        }
    }
}