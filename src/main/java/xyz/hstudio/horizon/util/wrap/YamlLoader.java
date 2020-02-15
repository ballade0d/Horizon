package xyz.hstudio.horizon.util.wrap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class YamlLoader extends YamlConfiguration {

    public static YamlLoader loadConfiguration(final File file) {
        YamlLoader config = new YamlLoader();
        try {
            config.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException var4) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, var4);
        }
        return config;
    }

    public static YamlConfiguration loadConfiguration(final InputStream stream) {
        YamlLoader config = new YamlLoader();
        try {
            config.load(new InputStreamReader(stream));
        } catch (IOException | InvalidConfigurationException var3) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", var3);
        }
        return config;
    }
}