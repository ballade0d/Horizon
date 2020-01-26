package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.Horizon;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.compat.IMcAccessor;
import xyz.hstudio.horizon.bukkit.compat.McAccessor;
import xyz.hstudio.horizon.bukkit.config.checks.HitBoxConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.HitBoxData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.AABB;
import xyz.hstudio.horizon.bukkit.util.Location;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.GameMode.ADVENTURE;
import static org.bukkit.GameMode.SURVIVAL;

public class HitBox extends Module<HitBoxData, HitBoxConfig> {

    public HitBox() {
        super(ModuleType.HitBox, new HitBoxConfig());
    }

    @Override
    public HitBoxData getData(final HoriPlayer player) {
        return player.hitBoxData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final HitBoxData data, final HitBoxConfig config) {
        typeA(event, player, data, config);
    }

    /**
     * A simple Reach check.
     * <p>
     * Accuracy: 8/10 - It may have some rare falses.
     * Efficiency: 10/10 - Detects reach instantly.
     *
     * @author MrCraftGoo, Islandscout
     */
    private void typeA(final Event event, final HoriPlayer player, final HitBoxData data, final HitBoxConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updatePos) {
                return;
            }
            data.history.add(new AbstractMap.SimpleEntry<>(e.to, System.currentTimeMillis()));
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
            if (player.player.getGameMode() != SURVIVAL && player.player.getGameMode() != ADVENTURE) {
                return;
            }
            Player targetPlayer = (Player) e.entity;
            HoriPlayer target = Horizon.PLAYERS.get(targetPlayer.getUniqueId());

            Location targetPos = new Location(targetPlayer.getLocation());
            if (target != null && this.getData(target).history.size() != 0) {
                // Get the history position to avoid false positives.
                targetPos = this.getHistoryLocation(player.ping, this.getData(target).history);
            }
            Vector move = targetPos.toVector().subtract(targetPlayer.getLocation().toVector());
            AABB targetCube = McAccessor.INSTANCE.getCube(targetPlayer).add(move);

            // Get player's eye position instead of feet position.
            Vector playerPos = player.getHeadPosition();

            // Use Euclidean Distance
            double reach = targetCube.distance(playerPos);

            if (reach > config.typeA_max_reach) {
                this.debug("Failed: TypeA, r:" + reach);

                // Punish
                this.punish(player, data, "TypeA", (reach - config.typeA_max_reach) * 10);
            } else {
                reward("TypeA", data, 0.995);
            }
        }
    }

    private Location getHistoryLocation(final long ping, final List<Map.Entry<Location, Long>> history) {
        long time = System.currentTimeMillis();
        for (int i = history.size() - 1; i >= 0; i--) {
            long elapsedTime = time - history.get(i).getValue();
            if (elapsedTime >= ping) {
                if (i == history.size() - 1) {
                    return history.get(i).getKey();
                }
                double nextMoveWeight = (elapsedTime - ping) / (double) (elapsedTime - (time - history.get(i + 1).getValue()));
                Location before = history.get(i).getKey();
                Location after = history.get(i + 1).getKey();
                Vector interpolate = after.toVector().subtract(before.toVector());
                interpolate.multiply(nextMoveWeight);
                before.add(interpolate);
                return before;
            }
        }
        return history.get(0).getKey();
    }
}