package xyz.hstudio.horizon.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.*;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class Listeners implements Listener {

    public Listeners() {
        Bukkit.getPluginManager().registerEvents(this, Horizon.getInst());
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        // Use callSyncMethod to fix an error if late-bind is enabled.
        Bukkit.getScheduler().callSyncMethod(Horizon.getInst(), () -> new HoriPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        Horizon.PLAYERS.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent e) {
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
    public void onRespawn(final PlayerRespawnEvent e) {
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
    public void onChangedWorld(final PlayerChangedWorldEvent e) {
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

    @EventHandler
    public void onPistonExtend(final BlockPistonExtendEvent e) {
        int length = e.getBlocks().size() + 1;
        Vector3D pos = new Vector3D(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
        Vector3D dir = pos.add(new Vector3D(e.getDirection().getModX() * length, e.getDirection().getModY() * length, e.getDirection().getModZ() * length));
        AABB aabb = new AABB(-0.1, -0.1, -0.1, 1.1, 1.1, 1.1).add(dir);
        Horizon.PLAYERS
                .values()
                .stream()
                .filter(p -> p.world.equals(e.getBlock().getWorld()))
                .forEach(p -> p.piston.add(aabb));
    }
}