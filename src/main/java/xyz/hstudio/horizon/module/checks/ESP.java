package xyz.hstudio.horizon.module.checks;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.api.events.inbound.SwingEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Ray;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.LinkedHashSet;
import java.util.Set;

public class ESP {

    private final Set<HoriPlayer> hiddenPlayers = new LinkedHashSet<>();

    private void handleMove(final MoveEvent e, final HoriPlayer player) {
        if (player.getPotionEffectAmplifier("INVISIBILITY") <= 0) {
            this.hiddenPlayers.remove(player);
        } else {
            this.hiddenPlayers.add(player);
        }
    }

    private void handleSwing(final SwingEvent e) {
        HoriPlayer player = e.player;
        for (final HoriPlayer target : this.hiddenPlayers) {
            if (!player.world.equals(target.world)) {
                continue;
            }
            // TODO: Ping handler
            AABB targetCube = McAccessor.INSTANCE.getCube(target.player);
            Vector3D headPos = player.getHeadPosition();
            Vector3D direction = MathUtils.getDirection(player.position.yaw, player.position.pitch);
            Ray ray = new Ray(headPos, direction);

            Vector3D intersection = targetCube.intersectsRay(ray, 0, 3.1F);

            if (intersection == null) {
                continue;
            }

            ((CraftPlayer) player.player).getHandle().attack(((CraftPlayer) target.player).getHandle());
        }
    }
}