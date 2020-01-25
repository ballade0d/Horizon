package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.ScaffoldConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.ScaffoldData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.TimeUtils;

import java.util.stream.DoubleStream;

public class Scaffold extends Module<ScaffoldData, ScaffoldConfig> {

    public Scaffold() {
        super(ModuleType.Scaffold, new ScaffoldConfig());
    }

    @Override
    public ScaffoldData getData(final HoriPlayer player) {
        return player.scaffoldData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {
        typeA(event, player, data, config);
        typeB(event, player, data, config);
        // TODO: SafeWalk check
    }

    /**
     * An easy packet check. This will detect some poorly made Scaffold/Tower.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positive.
     * Efficiency: 8/10 - Detects some Block hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            if (e.face == BlockPlaceEvent.BlockFace.INVALID) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 5);
                return;
            }
            Vector interaction = e.interaction;
            // Performance?
            if (DoubleStream.of(interaction.getX(), interaction.getY(), interaction.getZ())
                    .anyMatch(d -> d > 1 || d < 0)) {
                this.debug("Failed: TypeA, i:" + interaction.toString());

                // Punish
                this.punish(player, data, "TypeA", 5);
                return;
            }

            Block b = e.getTargetLocation().getBlock();
            String type = b == null ? null : b.getType().name();
            if (b != null && type.contains("AIR")) {
                this.debug("Failed: TypeA, t:" + type);

                // Punish
                this.punish(player, data, "TypeA", 5);
                return;
            }

            long deltaT = TimeUtils.now() - data.lastMove;
            if (!data.lagging && deltaT < 20) {
                if (++data.typeDFails > 4) {
                    this.debug("Failed: TypeA");

                    // Punish
                    this.punish(player, data, "TypeA", 3);
                    return;
                }
            } else if (data.typeDFails > 0) {
                data.typeDFails--;
            }
            reward("TypeA", data, 0.999);
        } else if (event instanceof MoveEvent) {
            long now = TimeUtils.now();
            data.lagging = now - data.lastMove < 5;
            data.lastMove = now;
        }
    }

    /**
     * An easy place angle check.
     * <p>
     * Accuracy: 9/10 - It may have a bit false positives.
     * Efficiency: 6/10 - Detects some poorly made Scaffold/Tower.
     */
    private void typeB(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            float angle = player.position.getDirection().angle(e.getPlaceBlockFace());
            if (angle > Math.toRadians(config.typeB_max_angle)) {
                this.debug("Failed: TypeB, a:" + angle);

                // Punish
                this.punish(player, data, "TypeB", 4);
            } else {
                reward("TypeB", data, 0.999);
            }
        }
    }
}