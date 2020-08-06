package xyz.hstudio.horizon.kirin.module;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;
import xyz.hstudio.horizon.event.outbound.VelocityEvent;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Kirin {

    public final List<CControl> cControls = new ArrayList<>();

    public Kirin(final Horizon horizon, final String name) throws Exception {
        Logger.msg("Kirin", "Verified successfully. Thanks for using Kirin!");
        Logger.msg("Kirin", "Hello " + name + " <3");

        File config = new File(horizon.getDataFolder(), "kirin.yml");
        if (!config.exists()) {
            Files.copy(horizon.getResource("kirin/kirin.yml"), config.toPath());
        }
        FileConfiguration yaml = YamlLoader.loadConfiguration(config);

        VelocityEvent.useExplosionPacket = yaml.getBoolean("use_explosion_packet", true);
    }
}