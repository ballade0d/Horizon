package xyz.hstudio.horizon.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.*;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

public class Listeners implements Listener {

    public Listeners() {
        Bukkit.getPluginManager().registerEvents(this, Horizon.getInst());
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
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
        player.teleports.put(new Location(e.getTo()), System.currentTimeMillis());
        player.world = e.getTo().getWorld();

        if (e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN &&
                Horizon.getInst().config.ghost_block_fix) {
            Location fromLoc = new Location(e.getFrom());
            Vector3D fromVec = new Vector3D(fromLoc.x, fromLoc.y, fromLoc.z);
            for (IWrappedBlock b : McAccessor.INSTANCE.getCube(p).translateTo(fromVec).getBlocks(fromLoc.world)) {
                McAccessor.INSTANCE.updateBlock(player, b.getPos());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(final PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        HoriPlayer player = Horizon.PLAYERS.get(p.getUniqueId());
        if (player == null) {
            return;
        }
        player.teleports.put(new Location(e.getRespawnLocation()), System.currentTimeMillis());
        player.world = e.getRespawnLocation().getWorld();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChangedWorld(final PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        HoriPlayer player = Horizon.PLAYERS.get(p.getUniqueId());
        if (player == null) {
            return;
        }
        player.teleports.put(new Location(p.getLocation()), System.currentTimeMillis());
        player.world = p.getWorld();
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