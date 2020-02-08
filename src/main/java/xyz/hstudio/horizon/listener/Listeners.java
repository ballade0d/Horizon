package xyz.hstudio.horizon.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.wrap.Location;

public class Listeners implements Listener {

    public Listeners() {
        Bukkit.getPluginManager().registerEvents(this, Horizon.getInst());
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // Use callSyncMethod to fix an error if late-bind is enabled.
        Bukkit.getScheduler().callSyncMethod(Horizon.getInst(), () -> new HoriPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Horizon.PLAYERS.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        HoriPlayer player = Horizon.PLAYERS.get(p.getUniqueId());
        if (player == null) {
            return;
        }
        player.isTeleporting = true;
        player.world = e.getTo().getWorld();
        player.teleportPos = new Location(e.getTo());
        player.teleportTime = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        HoriPlayer player = Horizon.PLAYERS.get(p.getUniqueId());
        if (player == null) {
            return;
        }
        player.isTeleporting = true;
        player.world = e.getRespawnLocation().getWorld();
        player.teleportPos = new Location(e.getRespawnLocation());
        player.teleportTime = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        HoriPlayer player = Horizon.PLAYERS.get(p.getUniqueId());
        if (player == null) {
            return;
        }
        player.isTeleporting = true;
        player.world = p.getWorld();
        player.teleportPos = new Location(e.getPlayer().getLocation());
        player.teleportTime = System.currentTimeMillis();
    }
}