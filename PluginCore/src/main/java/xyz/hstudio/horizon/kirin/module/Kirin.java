package xyz.hstudio.horizon.kirin.module;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.api.events.outbound.VelocityEvent;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Kirin {

    public Kirin(final Horizon horizon, final String name) throws Exception {
        Logger.msg("Kirin", "Verified successfully!");
        Logger.msg("Kirin", "Hello " + name + " <3");

        List<String> lines = new ArrayList<>();
        lines.add("# Should Horizon replace Velocity packet with Explosion packet?");
        lines.add("# This can make some anti velocity hacks unavailable.");
        lines.add("use_explosion_packet: true");

        File config = new File(horizon.getDataFolder(), "kirin.yml");
        if (!config.exists()) {
            FileWriter writer = new FileWriter(config);
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            writer.flush();
            writer.close();
        }
        FileConfiguration yaml = YamlLoader.loadConfiguration(config);

        VelocityEvent.useExplosionPacket = yaml.getBoolean("use_explosion_packet", true);
    }
}