package xyz.hstudio.horizon

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import xyz.hstudio.horizon.configuration.Config
import xyz.hstudio.horizon.task.Async
import xyz.hstudio.horizon.task.Sync
import xyz.hstudio.horizon.util.BlockUtils
import xyz.hstudio.horizon.util.Yaml
import xyz.hstudio.horizon.util.enums.Version
import java.io.File
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

class Horizon : JavaPlugin() {

    val players: MutableMap<UUID, HPlayer> = ConcurrentHashMap()

    val async: Async = Async(this)
    val sync: Sync = Sync(this)

    override fun onEnable() {
        // Async task
        async.start()

        // Sync task
        sync.start()

        // Load static code block
        BlockUtils.isSolid(Material.AIR)

        // Load the config file
        val folder = dataFolder
        val configFile = File(folder, "config.yml")
        if (!folder.isDirectory || !configFile.isFile) {
            saveResource("config.yml", true)
        }
        Config.load(Yaml.loadConfiguration(configFile))

        // Register for joined players
        Bukkit.getOnlinePlayers().forEach { bukkit: Player? -> HPlayer(bukkit) }

        // Register when player joins
        Bukkit.getPluginManager().registerEvents(object : Listener {
            @EventHandler
            fun onJoin(e: PlayerJoinEvent) {
                HPlayer(e.player)
            }
        }, this)
    }

    override fun onDisable() {
        // Unregister packet handlers
        players.values.forEach(Consumer { p: HPlayer -> p.packetHandler.unregister() })
        players.clear()

        // Stop the tasks
        async.cancel()
        sync.cancel()

        // Release the jar
        try {
            (classLoader as URLClassLoader).close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        // Check the server version first
        check(!(Version.inst === Version.UNKNOWN)) { "Unsupported version!" }
    }
}