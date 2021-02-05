package xyz.hstudio.horizon.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Yaml extends YamlConfiguration {

    public static Yaml loadConfiguration(File file) {
        Yaml config = new Yaml();
        try {
            config.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException ignore) {
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, e);
        }
        return config;
    }

    public static Yaml loadConfiguration(InputStream stream) {
        Yaml config = new Yaml();
        try {
            config.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", e);
        }
        return config;
    }
}