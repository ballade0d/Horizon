package xyz.hstudio.horizon.module.checks;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.HitBoxData;
import xyz.hstudio.horizon.file.node.HitBoxNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class HitBox extends Module<HitBoxData, HitBoxNode> {

    public HitBox() {
        super(ModuleType.HitBox, new HitBoxNode(), "TypeA");
    }

    @Override
    public HitBoxData getData(final HoriPlayer player) {
        return player.hitBoxData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final HitBoxData data, final HitBoxNode config) {
        event.setCancelled(true);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final HitBoxData data, final HitBoxNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    /**
     * A simple Reach check.
     * <p>
     * Accuracy: 8/10 - It may have some rare falses.
     * Efficiency: 10/10 - Detects reach instantly.
     *
     * @author MrCraftGoo, Islandscout
     */
    private void typeA(final Event event, final HoriPlayer player, final HitBoxData data, final HitBoxNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updatePos) {
                return;
            }
            data.history.add(new Pair<>(e.to, System.currentTimeMillis()));
            if (data.history.size() > 30) {
                data.history.remove(0);
            }
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            if (!(e.entity instanceof Player)) {
                return;
            }
            if (player.player.getGameMode() != GameMode.SURVIVAL && player.player.getGameMode() != GameMode.ADVENTURE) {
                return;
            }
            Player targetPlayer = (Player) e.entity;
            HoriPlayer target = Horizon.PLAYERS.get(targetPlayer.getUniqueId());

            Location targetPos = new Location(targetPlayer.getLocation());
            if (target != null && this.getData(target).history.size() != 0) {
                // Get the history position to avoid false positives.
                targetPos = this.getData(target).getHistoryLocation(player.ping, true);
            }
            Vector3D move = targetPos.toVector().subtract(targetPlayer.getLocation().toVector());
            AABB targetCube = McAccessor.INSTANCE.getCube(targetPlayer).add(move);

            // Get player's eye position instead of feet position.
            Vector3D playerPos = player.getHeadPosition();

            // Use Euclidean Distance
            double reach = targetCube.distance(playerPos);

            if (reach > config.typeA_max_reach) {
                // Punish
                this.punish(event, player, data, 0, (float) ((reach - config.typeA_max_reach) * 10));
            } else {
                reward(0, data, 0.995);
            }
        }
    }
}