package xyz.hstudio.horizon.configuration;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import me.cgoo.api.logger.Logger;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.hstudio.horizon.api.enums.Detection;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Execution {

    private static final Map<Detection, TIntObjectMap<List<String>>> collection = new EnumMap<>(Detection.class);
    private static final TIntObjectMap<List<String>> EMPTY = new TIntObjectHashMap<>(0);

    public static TIntObjectMap<List<String>> getActionMap(Detection detection) {
        return collection.getOrDefault(detection, EMPTY);
    }

    public static void load(FileConfiguration config) {
        for (Detection detection : Detection.values()) {
            String name = detection.name();
            if (!config.isConfigurationSection(name)) {
                continue;
            }
            TIntObjectMap<List<String>> action = new TIntObjectHashMap<>();
            for (String vl : config.getConfigurationSection(name).getKeys(false)) {
                try {
                    action.put(Integer.parseInt(vl), config.getStringList(name + "." + vl));
                } catch (NumberFormatException e) {
                    Logger.warn("Failed to load the execution of " + name + " ! Reason: '" + vl + "' is not a integer.");
                }
            }
            collection.put(detection, action);
        }
    }
}