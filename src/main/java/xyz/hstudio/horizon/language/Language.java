package xyz.hstudio.horizon.language;

import me.cgoo.api.cfg.LoadPath;
import xyz.hstudio.horizon.HPlayer;

import java.util.HashMap;
import java.util.Map;

public class Language {

    @LoadPath("no_permission")
    public String NO_PERMISSION;

    @LoadPath("cmd_bungee_executed")
    public String CMD_BUNGEE_EXECUTED;
    @LoadPath("cmd_bungee_no_online")
    public String CMD_BUNGEE_NO_ONLINE;

    private static final Map<String, Language> LANGUAGES = new HashMap<>();

    public Language(String name) {
        LANGUAGES.put(name, this);
    }

    public static Language getLang(HPlayer p) {
        return LANGUAGES.getOrDefault(p.locale, LANGUAGES.get("en_US"));
    }

    public static Language getLang(String name) {
        return LANGUAGES.getOrDefault(name, LANGUAGES.get("en_US"));
    }
}