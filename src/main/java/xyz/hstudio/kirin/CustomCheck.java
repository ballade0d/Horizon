package xyz.hstudio.kirin;

import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.Logger;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomCheck {

    private static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    private final Invocable invocable;
    private final String name;
    private final String author;
    private final String version;

    public CustomCheck(File script) throws IOException, ScriptException, NoSuchMethodException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(FileUtils.readFileToString(script, StandardCharsets.UTF_8));
        invocable = (Invocable) engine;
        name = invocable.invokeFunction("getName").toString();
        author = invocable.invokeFunction("getAuthor").toString();
        version = invocable.invokeFunction("getVersion").toString();
        Logger.msg("Kirin", "Check " + name + " " + version + " by " + author + " is successfully loaded.");
    }

    public static void init() {
        File folder = new File(inst.getDataFolder(), "scripts");
        if (!folder.exists()) {
            folder.mkdirs();
            return;
        }

        File[] scripts = folder.listFiles();
        if (scripts == null) {
            return;
        }

        for (File script : scripts) {
            try {
                new CustomCheck(script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}