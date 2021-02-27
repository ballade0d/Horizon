package xyz.hstudio.kirin.module;

import me.cgoo.api.logger.Logger;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hstudio.horizon.Horizon;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Script {

    private static final Horizon inst = JavaPlugin.getPlugin(Horizon.class);

    public static final Map<String, Invocable> CHECKS = new ConcurrentHashMap<>();

    private Script(File script) throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.eval(FileUtils.readFileToString(script, StandardCharsets.UTF_8));
        if (!(engine instanceof Invocable)) {
            throw new IllegalStateException("not a Invocable");
        }
        Invocable invocable = (Invocable) engine;
        String name = invocable.invokeFunction("getName").toString();
        String author = invocable.invokeFunction("getAuthor").toString();
        String version = invocable.invokeFunction("getVersion").toString();

        if (CHECKS.containsKey(name)) {
            Logger.log("Kirin", "Script " + name + " already exists! Kirin will only load the first one.");
            return;
        }
        Logger.log("Kirin", "Script " + name + "(" + version + ") by " + author + " is successfully loaded.");

        CHECKS.put(name, invocable);
    }

    public static void init() {
        File folder = new File(inst.getDataFolder(), "kirin/scripts");
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
                new Script(script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}