package xyz.hstudio.horizon.module.checks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.ESPData;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.file.node.ESPNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.Set;
import java.util.UUID;

public class ESP extends Module<ESPData, ESPNode> {

    public ESP() {
        super(ModuleType.ESP, new ESPNode());
    }

    @Override
    public ESPData getData(final HoriPlayer player) {
        return player.espData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final ESPData data, final ESPNode config) {
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final ESPData data, final ESPNode config) {
    }

    @Override
    public void tickAsync(final long currentTick, final ESPNode config) {
        if (currentTick % 2 != 0) {
            return;
        }
        for (HoriPlayer observer : Horizon.PLAYERS.values()) {
            if (this.canBypass(observer)) {
                showAll(observer, this.getData(observer).hiddenPlayers);
                continue;
            }
            if (observer.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                showAll(observer, this.getData(observer).hiddenPlayers);
                continue;
            }
            for (HoriPlayer player : Horizon.PLAYERS.values()) {
                if (!player.world.getUID().equals(observer.world.getUID())) {
                    continue;
                }
                if (player.position.distance(observer.position) > config.max_distance) {
                    continue;
                }
                if (player.getPlayer().getUniqueId().equals(observer.getPlayer().getUniqueId())) {
                    continue;
                }
                if (player.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
                ESPData data = this.getData(observer);
                if (!isTargetPosInSight(observer, player.getHeadPosition(), config.check_angle)) {
                    // Hide
                    if (data.hiddenPlayers.contains(player.getPlayer().getUniqueId())) {
                        continue;
                    }
                    McAccessor.INSTANCE.ensureMainThread(() -> McAccessor.INSTANCE.hidePlayer(observer.getPlayer(), player.getPlayer()));
                    data.hiddenPlayers.add(player.getPlayer().getUniqueId());
                } else {
                    // Show
                    if (!data.hiddenPlayers.contains(player.getPlayer().getUniqueId())) {
                        continue;
                    }
                    McAccessor.INSTANCE.ensureMainThread(() -> McAccessor.INSTANCE.showPlayer(observer.getPlayer(), player.getPlayer()));
                    data.hiddenPlayers.remove(player.getPlayer().getUniqueId());
                }
            }
        }
    }

    private boolean isTargetPosInSight(final HoriPlayer observer, final Vector3D target, final boolean checkAngle) {
        Vector3D eyePos = observer.getHeadPosition();
        Vector3D ray = target.subtract(eyePos);
        Vector3D lookingVec = observer.position.getDirection();

        float angle = lookingVec.angle(ray);
        if (checkAngle && Math.toDegrees(angle) > 90) {
            return false;
        }
        double checkTimes = ray.length() / 0.5;
        if (checkTimes < 1) {
            checkTimes = 1;
        }
        ray.multiply(1 / checkTimes);
        for (; checkTimes > 0; checkTimes--) {
            if (!eyePos.toLocation(observer.world).getBlock().getType().isTransparent()) {
                return false;
            }
            eyePos.add(ray);
        }
        return true;
    }

    private void showAll(final HoriPlayer observer, final Set<UUID> uuidSet) {
        for (UUID uuid : uuidSet) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            McAccessor.INSTANCE.ensureMainThread(() -> McAccessor.INSTANCE.showPlayer(observer.getPlayer(), player));
        }
        uuidSet.clear();
    }
}