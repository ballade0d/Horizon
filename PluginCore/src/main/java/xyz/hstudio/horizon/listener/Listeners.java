package xyz.hstudio.horizon.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.inbound.SyncWindowClickEvent;
import xyz.hstudio.horizon.kirin.module.CControl;
import xyz.hstudio.horizon.module.Module;
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
        Bukkit.getScheduler().runTask(Horizon.getInst(), () -> {
            try {
                HoriPlayer player = new HoriPlayer(event.getPlayer());
                if (Horizon.getInst().kirin == null) {
                    return;
                }
                for (CControl cControl : Horizon.getInst().kirin.cControls) {
                    cControl.onJoin(player);
                }
            } catch (Throwable ignore) {
            }
        });
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        Horizon.PLAYERS.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(final PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        HoriPlayer player = Horizon.PLAYERS.get(p.getUniqueId());
        if (player == null) {
            return;
        }

        if (e.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN &&
                Horizon.getInst().config.ghost_block_fix) {
            Location fromLoc = new Location(e.getFrom());
            Vector3D fromVec = new Vector3D(fromLoc.x, fromLoc.y, fromLoc.z);
            for (IWrappedBlock b : McAccessor.INSTANCE.getCube(p).translateTo(fromVec).getBlocks(fromLoc.world)) {
                McAccessor.INSTANCE.updateBlock(player, b.getPos());
            }
        }
    }

    @EventHandler
    public void onPistonExtend(final BlockPistonExtendEvent e) {
        int length = e.getBlocks().size() + 2;
        BlockFace face = e.getDirection();
        Vector3D pos = new Vector3D(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
        AABB aabb = new AABB(-0.1, -0.1, -0.1, 1.1, 1.1, 1.1)
                .add(0, 0, 0, length * face.getModX(), length * face.getModY(), length * face.getModZ())
                .add(pos);
        Horizon.PLAYERS
                .values()
                .stream()
                .filter(p -> p.getWorld().equals(e.getBlock().getWorld()))
                .forEach(p -> p.piston.add(aabb));
    }

    @EventHandler
    public void onPistonRetract(final BlockPistonRetractEvent e) {
        int length = e.getBlocks().size() + 1;
        BlockFace face = e.getDirection();
        Vector3D pos = new Vector3D(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ());
        AABB aabb = new AABB(-0.1, -0.1, -0.1, 1.1, 1.1, 1.1)
                .add(0, 0, 0, length * face.getModX(), length * face.getModY(), length * face.getModZ())
                .add(pos);
        aabb.highlight(e.getBlock().getWorld(), 0.3);
        Horizon.PLAYERS
                .values()
                .stream()
                .filter(p -> p.getWorld().equals(e.getBlock().getWorld()))
                .forEach(p -> p.piston.add(aabb));
    }

    // TODO: Make this async?
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        HoriPlayer player = Horizon.PLAYERS.get(e.getWhoClicked().getUniqueId());
        if (player == null) {
            return;
        }
        SyncWindowClickEvent event = new SyncWindowClickEvent(player, e.getView(), e.getSlotType(), e.getRawSlot(), e.getClick(), e.getAction(), e.getHotbarButton());
        Module.doCheck(event, player);
    }
}