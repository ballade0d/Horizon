package xyz.hstudio.horizon.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;

public class Listeners implements Listener {

    public Listeners() {
        Bukkit.getPluginManager().registerEvents(this, Horizon.getInst());
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        new HoriPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Horizon.PLAYERS.remove(event.getPlayer().getUniqueId());
    }
}