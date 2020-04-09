package xyz.hstudio.kirinserver;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.hstudio.horizon.Horizon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Module {

    public Module(final Horizon horizon) throws Exception {
        Bukkit.getConsoleSender().sendMessage("(H|Kirin) Verified successfully!");
        Bukkit.getConsoleSender().sendMessage("(H|Kirin) Hello {user} <3");

        List<String> lines = new ArrayList<>();
        lines.add("# Should Horizon replace Velocity packet with Explosion packet?");
        lines.add("# This can make some anti velocity hacks unavailable.");
        lines.add("use_explosion_packet: true");

        File config = new File(horizon.getDataFolder(), "kirin.yml");
        if (!config.exists()) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(config));
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        }

        FileConfiguration yaml = YamlConfiguration.loadConfiguration(config);


        Method method = Arrays
                .stream(Horizon.class.getMethods())
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .findFirst()
                .orElse(null);
        if (method == null) {
            throw new IllegalStateException("Failed to find necessary method for Kirin to run!");
        }

        method.invoke(null, "use_explosion_packet", yaml.getBoolean("use_explosion_packet", true));
    }
}