package xyz.hstudio.horizon.file;

import org.bukkit.ChatColor;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.util.Arrays;
import java.util.List;

public class ConfigFile extends AbstractFile {

    @Load(path = "prefix")
    public String prefix = "§9§lHorizon §1§l>> §r§3";

    @Load(path = "log")
    public boolean log = true;

    @Load(path = "command_alias")
    public List<String> command_alias = Arrays.asList("hz", "hori");

    @Load(path = "op_bypass")
    public boolean op_bypass = false;

    @Load(path = "personalized_themes.enabled")
    public boolean personalized_themes_enabled = true;
    @Load(path = "personalized_themes.default_lang")
    public String personalized_themes_default_lang = "en_US";

    @Load(path = "kirin.enabled")
    public boolean kirin_enabled = false;
    @Load(path = "kirin.licence")
    public String kirin_licence = "";

    @Load(path = "ghost_block_fix")
    public boolean ghost_block_fix = true;

    @Override
    public Object getValue(final String path, final YamlLoader loader, final Class<?> type) {
        Object value = loader.get(path);
        if (value instanceof String) {
            return ChatColor.translateAlternateColorCodes('&', (String) value);
        }
        return value;
    }
}