package xyz.hstudio.horizon;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.hstudio.horizon.command.Executors;
import xyz.hstudio.horizon.compat.IBot;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.file.AbstractFile;
import xyz.hstudio.horizon.file.ConfigFile;
import xyz.hstudio.horizon.file.LangFile;
import xyz.hstudio.horizon.kirin.module.Kirin;
import xyz.hstudio.horizon.kirin.verification.Verification;
import xyz.hstudio.horizon.listener.Listeners;
import xyz.hstudio.horizon.menu.MenuListener;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.network.ChannelHandler;
import xyz.hstudio.horizon.thread.Async;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.enums.Version;
import xyz.hstudio.horizon.util.wrap.YamlLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Horizon extends JavaPlugin {

    // This is a recoding version.
    // You can contribute us if you want to help horizon become the best anticheat.

    public static final Map<UUID, HoriPlayer> PLAYERS = new ConcurrentHashMap<>();
    @Getter
    private static Horizon inst;

    public final Map<String, LangFile> langMap = new ConcurrentHashMap<>();
    public YamlLoader checkLoader;
    public ConfigFile config;
    private boolean usePapi;
    public boolean useViaVer;
    public boolean usePSupport;
    public Kirin kirin;

    private BukkitTask syncTask;
    private Async asyncTask;

    private final byte[] shit = new byte[]{
            -54, -2, -70, -66, 0, 0, 0, 52, 0, 64, 10, 0, 35, 0, 45, 7, 0, 46, 10, 0, 2, 0,
            45, 7, 0, 47, 10, 0, 4, 0, 45, 7, 0, 48, 10, 0, 6, 0, 45, 7, 0, 49, 10, 0, 8, 0,
            45, 7, 0, 50, 10, 0, 10, 0, 45, 7, 0, 51, 10, 0, 12, 0, 45, 7, 0, 52, 10, 0, 14,
            0, 45, 7, 0, 53, 10, 0, 16, 0, 45, 7, 0, 54, 10, 0, 18, 0, 45, 7, 0, 55, 10, 0,
            20, 0, 45, 7, 0, 56, 10, 0, 22, 0, 45, 7, 0, 57, 10, 0, 24, 0, 45, 7, 0, 58, 10,
            0, 26, 0, 45, 7, 0, 59, 10, 0, 28, 0, 45, 7, 0, 60, 10, 0, 30, 0, 45, 7, 0, 61,
            10, 0, 32, 0, 45, 7, 0, 62, 7, 0, 63, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0,
            3, 40, 41, 86, 1, 0, 4, 67, 111, 100, 101, 1, 0, 15, 76, 105, 110, 101, 78, 117,
            109, 98, 101, 114, 84, 97, 98, 108, 101, 1, 0, 18, 76, 111, 99, 97, 108, 86, 97,
            114, 105, 97, 98, 108, 101, 84, 97, 98, 108, 101, 1, 0, 4, 116, 104, 105, 115, 1,
            0, 6, 76, 83, 104, 105, 116, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105,
            108, 101, 1, 0, 9, 83, 104, 105, 116, 46, 106, 97, 118, 97, 12, 0, 36, 0, 37, 1,
            0, 41, 120, 121, 122, 47, 104, 115, 116, 117, 100, 105, 111, 47, 104, 111, 114,
            105, 122, 111, 110, 47, 109, 111, 100, 117, 108, 101, 47, 99, 104, 101, 99, 107,
            115, 47, 65, 110, 116, 105, 66, 111, 116, 1, 0, 46, 120, 121, 122, 47, 104, 115,
            116, 117, 100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111, 100,
            117, 108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 65, 110, 116, 105, 86, 101, 108,
            111, 99, 105, 116, 121, 1, 0, 45, 120, 121, 122, 47, 104, 115, 116, 117, 100, 105,
            111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111, 100, 117, 108, 101, 47,
            99, 104, 101, 99, 107, 115, 47, 65, 117, 116, 111, 67, 108, 105, 99, 107, 101, 114,
            1, 0, 43, 120, 121, 122, 47, 104, 115, 116, 117, 100, 105, 111, 47, 104, 111, 114,
            105, 122, 111, 110, 47, 109, 111, 100, 117, 108, 101, 47, 99, 104, 101, 99, 107,
            115, 47, 66, 97, 100, 80, 97, 99, 107, 101, 116, 1, 0, 37, 120, 121, 122, 47, 104,
            115, 116, 117, 100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111,
            100, 117, 108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 69, 83, 80, 1, 0, 45, 120,
            121, 122, 47, 104, 115, 116, 117, 100, 105, 111, 47, 104, 111, 114, 105, 122, 111,
            110, 47, 109, 111, 100, 117, 108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 71, 114,
            111, 117, 110, 100, 83, 112, 111, 111, 102, 1, 0, 43, 120, 121, 122, 47, 104, 115,
            116, 117, 100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111, 100,
            117, 108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 72, 101, 97, 108, 116, 104, 84,
            97, 103, 1, 0, 40, 120, 121, 122, 47, 104, 115, 116, 117, 100, 105, 111, 47, 104,
            111, 114, 105, 122, 111, 110, 47, 109, 111, 100, 117, 108, 101, 47, 99, 104, 101,
            99, 107, 115, 47, 72, 105, 116, 66, 111, 120, 1, 0, 47, 120, 121, 122, 47, 104,
            115, 116, 117, 100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111,
            100, 117, 108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 73, 110, 118, 97, 108, 105,
            100, 77, 111, 116, 105, 111, 110, 1, 0, 43, 120, 121, 122, 47, 104, 115, 116, 117,
            100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111, 100, 117, 108,
            101, 47, 99, 104, 101, 99, 107, 115, 47, 73, 110, 118, 101, 110, 116, 111, 114,
            121, 1, 0, 42, 120, 121, 122, 47, 104, 115, 116, 117, 100, 105, 111, 47, 104, 111,
            114, 105, 122, 111, 110, 47, 109, 111, 100, 117, 108, 101, 47, 99, 104, 101, 99,
            107, 115, 47, 75, 105, 108, 108, 65, 117, 114, 97, 1, 0, 45, 120, 121, 122, 47,
            104, 115, 116, 117, 100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47,
            109, 111, 100, 117, 108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 75, 105, 108,
            108, 65, 117, 114, 97, 66, 111, 116, 1, 0, 41, 120, 121, 122, 47, 104, 115, 116,
            117, 100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111, 100,
            117, 108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 78, 111, 83, 119, 105, 110,
            103, 1, 0, 42, 120, 121, 122, 47, 104, 115, 116, 117, 100, 105, 111, 47, 104,
            111, 114, 105, 122, 111, 110, 47, 109, 111, 100, 117, 108, 101, 47, 99, 104,
            101, 99, 107, 115, 47, 73, 110, 116, 101, 114, 97, 99, 116, 1, 0, 39, 120,
            121, 122, 47, 104, 115, 116, 117, 100, 105, 111, 47, 104, 111, 114, 105, 122,
            111, 110, 47, 109, 111, 100, 117, 108, 101, 47, 99, 104, 101, 99, 107, 115,
            47, 83, 112, 101, 101, 100, 1, 0, 39, 120, 121, 122, 47, 104, 115, 116, 117,
            100, 105, 111, 47, 104, 111, 114, 105, 122, 111, 110, 47, 109, 111, 100, 117,
            108, 101, 47, 99, 104, 101, 99, 107, 115, 47, 84, 105, 109, 101, 114, 1, 0,
            4, 83, 104, 105, 116, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47,
            79, 98, 106, 101, 99, 116, 0, 33, 0, 34, 0, 35, 0, 0, 0, 0, 0, 1, 0, 1, 0,
            36, 0, 37, 0, 1, 0, 38, 0, 0, 0, -13, 0, 2, 0, 1, 0, 0, 0, -123, 42, -73, 0,
            1, -69, 0, 2, 89, -73, 0, 3, 87, -69, 0, 4, 89, -73, 0, 5, 87, -69, 0, 6, 89,
            -73, 0, 7, 87, -69, 0, 8, 89, -73, 0, 9, 87, -69, 0, 10, 89, -73, 0, 11, 87,
            -69, 0, 12, 89, -73, 0, 13, 87, -69, 0, 14, 89, -73, 0, 15, 87, -69, 0, 16,
            89, -73, 0, 17, 87, -69, 0, 18, 89, -73, 0, 19, 87, -69, 0, 20, 89, -73, 0,
            21, 87, -69, 0, 22, 89, -73, 0, 23, 87, -69, 0, 24, 89, -73, 0, 25, 87, -69,
            0, 26, 89, -73, 0, 27, 87, -69, 0, 28, 89, -73, 0, 29, 87, -69, 0, 30, 89,
            -73, 0, 31, 87, -69, 0, 32, 89, -73, 0, 33, 87, -79, 0, 0, 0, 2, 0, 39, 0,
            0, 0, 74, 0, 18, 0, 0, 0, 5, 0, 4, 0, 6, 0, 12, 0, 7, 0, 20, 0, 8, 0, 28, 0,
            9, 0, 36, 0, 10, 0, 44, 0, 11, 0, 52, 0, 12, 0, 60, 0, 13, 0, 68, 0, 14, 0,
            76, 0, 15, 0, 84, 0, 16, 0, 92, 0, 17, 0, 100, 0, 18, 0, 108, 0, 19, 0, 116,
            0, 20, 0, 124, 0, 21, 0, -124, 0, 22, 0, 40, 0, 0, 0, 12, 0, 1, 0, 0, 0, -123,
            0, 41, 0, 42, 0, 0, 0, 1, 0, 43, 0, 0, 0, 2, 0, 44
    };

    public Horizon() {
        Horizon.inst = this;
    }

    @Override
    public void onEnable() {
        if (Version.VERSION == Version.UNKNOWN) {
            // Unknown version
            Logger.msg("Error", "Your server version is not supported!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        File folder = this.getDataFolder();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Load config
        File checkFile = new File(folder, "check.yml");
        if (!checkFile.exists()) {
            saveResource("check.yml", true);
        }
        this.checkLoader = YamlLoader.loadConfiguration(checkFile);

        File configFile = new File(folder, "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", true);
        }
        YamlLoader configYaml = YamlLoader.loadConfiguration(configFile);
        this.config = AbstractFile.load(null, new ConfigFile(), configYaml);

        this.langMap.put("original", AbstractFile.load(null, new LangFile(), YamlLoader.loadConfiguration(this.getResource("lang.yml"))));
        // TODO: Load languages from cloud

        this.usePapi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        this.useViaVer = Bukkit.getPluginManager().isPluginEnabled("ViaVersion");
        this.usePSupport = Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport");

        if (this.config.kirin_enabled) {
            saveResource("pubKey.key", true);
            try {
                this.kirin = new Verification(new File(folder, "pubKey.key"), this.config.kirin_licence).connect();
            } catch (Exception e) {
                Logger.msg("Kirin", "Failed to init Kirin! Please contact the author for help.");
                e.printStackTrace();
            }
        }

        new MetricsLite(this, 4236);

        // Run every 50ms (1 tick)
        this.asyncTask = new Async();
        this.syncTask = Bukkit.getScheduler().runTaskTimer(this, new Sync(), 1L, 1L);

        new Listeners();
        new MenuListener();
        new Executors();
        // Load static code block
        BlockUtils.isSolid(Material.AIR);

        // Enable checks
        try {
            this.loadClass(shit).newInstance();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        Bukkit.getOnlinePlayers().forEach(HoriPlayer::new);

        Async.LOG.addLast("Horizon is fully startup.");
    }

    @Override
    public void onDisable() {
        Horizon.PLAYERS.values().forEach(player -> {
            IBot bot = player.killAuraBotData.bot;
            if (bot != null) {
                bot.despawn(player);
                player.killAuraBotData.bot = null;
            }
            ChannelHandler.unregister(player);
        });
        Horizon.PLAYERS.clear();

        Module.MODULE_MAP.clear();

        if (this.syncTask != null) {
            this.syncTask.cancel();
        }
        this.asyncTask.running = false;

        try {
            ((URLClassLoader) this.getClassLoader()).close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        File checkFile = new File(this.getDataFolder(), "check.yml");
        this.checkLoader = YamlLoader.loadConfiguration(checkFile);

        File configFile = new File(this.getDataFolder(), "config.yml");
        YamlLoader configYaml = YamlLoader.loadConfiguration(configFile);
        this.config = AbstractFile.load(null, new ConfigFile(), configYaml);

        try {
            this.loadClass(shit).newInstance();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public LangFile getLang(final String name) {
        return config.personalized_themes_enabled ?
                langMap.getOrDefault(name, langMap.get("original")) :
                langMap.get("original");
    }

    public String applyPAPI(final Player player, final String origin) {
        return this.usePapi ? PlaceholderAPI.setPlaceholders(player, origin) : origin;
    }

    public Class<?> loadClass(final byte[] code) throws Throwable {
        Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        define.setAccessible(true);
        Class<?> result;
        try {
            result = (Class<?>) define.invoke(this.getClassLoader(), null, code, 0, code.length);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
        return result;
    }
}