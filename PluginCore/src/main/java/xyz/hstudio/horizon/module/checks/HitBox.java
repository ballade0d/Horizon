package xyz.hstudio.horizon.module.checks;

import org.bukkit.GameMode;
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
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.collect.Pair;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class HitBox extends Module<HitBoxData, HitBoxNode> {

    public HitBox() {
        super(ModuleType.HitBox, new HitBoxNode(), "Reach", "Direction");
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
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updatePos) {
                return;
            }
            data.history.add(new Pair<>(e.to, System.currentTimeMillis()));
            if (data.history.size() > 30) {
                data.history.remove(0);
            }
        }
        if (config.reach_enabled) {
            typeA(event, player, data, config);
        }
        if (config.direction_enabled) {
            typeB(event, player, data, config);
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
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            if (player.getPlayer().getGameMode() != GameMode.SURVIVAL && player.getPlayer().getGameMode() != GameMode.ADVENTURE) {
                return;
            }
            HoriPlayer target = Horizon.PLAYERS.get(e.entity.getUniqueId());
            if (target == null) {
                return;
            }

            Location targetPos = target.position;
            if (this.getData(target).history.size() != 0) {
                // Get the history position to avoid false positives.
                targetPos = this.getData(target).getHistoryLocation(McAccessor.INSTANCE.getPing(player.getPlayer()), true);
            }
            Vector3D move = targetPos.toVector().subtract(target.position.toVector());
            AABB targetCube = McAccessor.INSTANCE.getCube(e.entity).add(move);

            // Get player's eye position instead of feet position.
            Vector3D playerPos = player.getHeadPosition();

            // Use Euclidean Distance
            double reach = targetCube.distance(playerPos);

            if (reach > config.reach_max_reach) {
                // Punish
                this.punish(event, player, data, 0, (float) ((reach - config.reach_max_reach) * 10), "d:" + reach);
            } else {
                reward(0, data, 0.99);
            }
        }
    }

    /**
     * A Direction check.
     * <p>
     * Accuracy: 8/10 - It may have some rare falses.
     * Efficiency: 10/10 - Detects hitbox instantly.
     *
     * @author MrCraftGoo, Islandscout
     */
    private void typeB(final Event event, final HoriPlayer player, final HitBoxData data, final HitBoxNode config) {
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            HoriPlayer target = Horizon.PLAYERS.get(e.entity.getUniqueId());
            if (target == null) {
                return;
            }

            Vector3D playerPos = player.getHeadPosition();
            Vector3D dir = player.position.getDirection();
            Vector3D extraDir = MathUtils.getDirection(player.position.yaw + data.deltaYaw, player.position.pitch + data.deltaPitch);

            Location targetPos = target.position;
            if (this.getData(target).history.size() != 0) {
                // Get the history position to avoid false positives.
                targetPos = this.getData(target).getHistoryLocation(McAccessor.INSTANCE.getPing(player.getPlayer()), true);
            }
            Vector3D move = targetPos.toVector().subtract(target.position.toVector());
            AABB targetCube = McAccessor.INSTANCE.getCube(e.entity).add(move);
            targetCube = targetCube.expand(0.3, 0.3, 0.3);

            if (targetCube.distance(playerPos) > 0.5 && !targetCube.betweenRays(playerPos, dir, extraDir)) {
                // Punish
                this.punish(event, player, data, 1, 3);
            } else {
                reward(1, data, 0.99);
            }
        } else if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            data.deltaYaw = e.to.yaw - e.from.yaw;
            data.deltaPitch = e.to.pitch - e.from.pitch;
        }
    }
}