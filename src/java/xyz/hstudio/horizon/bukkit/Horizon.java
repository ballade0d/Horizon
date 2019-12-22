package xyz.hstudio.horizon.bukkit;

import lombok.Getter;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.util.YamlLoader;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Horizon {

    // This is a recoding version.
    // You can contribute us if you want to help horizon become the best anticheat.

    public static Map<UUID, HoriPlayer> playerMap = new ConcurrentHashMap<>();
    @Getter
    private static Horizon inst;
    public Map<String, YamlLoader> configMap = new ConcurrentHashMap<>();

    public Horizon() {
        Horizon.inst = this;
    }
}