package xyz.hstudio.horizon.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Yaml extends YamlConfiguration {

    public static Yaml loadConfiguration(File file) {
        Yaml config = new Yaml();
        try {
            config.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
        return config;
    }

    public static Yaml loadConfiguration(InputStream stream) {
        Yaml config = new Yaml();
        try {
            config.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
        return config;
    }
}