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
import xyz.hstudio.horizon.bukkit.network.events.inbound.HeldItemEvent;
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
    }

    /**
     * An easy packet check. This will detect some poorly made Scaffold/Tower.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positive.
     * Efficiency: 7/10 - Detects some Block hacks instantly.
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
                this.punish(player, data, "TypeA", 0);
            }
            Vector interaction = e.interaction;
            // Performance?
            if (DoubleStream.of(interaction.getX(), interaction.getY(), interaction.getZ())
                    .anyMatch(d -> d > 1 || d < 0)) {
                this.debug("Failed: TypeB, i:" + interaction.toString());

                // Punish
                this.punish(player, data, "TypeB", 0);
            }

            Block b = e.getTargetLocation().getBlock();
            if (b != null && b.getType().name().contains("AIR")) {
                this.debug("Failed: TypeC");

                // Punish
                this.punish(player, data, "TypeC", 0);
            }

            long deltaT = TimeUtils.now() - data.lastMove;
            if (!data.lagging && deltaT < 20) {
                if (++data.typeDFails > 4) {
                    this.debug("Failed: TypeD");

                    // Punish
                    this.punish(player, data, "TypeD", 0);
                }
            } else if (data.typeDFails > 0) {
                data.typeDFails--;
            }
        } else if (event instanceof MoveEvent) {
            long now = TimeUtils.now();
            data.lagging = now - data.lastMove < 5;
            data.lastMove = now;
        }
    }

    /**
     * An AutoBlock/AutoSwitch check.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positive.
     * Efficiency: 8/10 - Detects most autoswitch instantly.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            data.lastPlaceTick = player.currentTick;
        } else if (event instanceof HeldItemEvent) {
            // TODO: Remember to skip 1.9+ client.
            if (player.currentTick - data.lastPlaceTick <= 1) {
                this.debug("Failed: TypeE");

                // Punish
                this.punish(player, data, "TypeD", 0);
            }
        }
    }

    /**
     * An easy place angle check.
     * <p>
     * Accuracy: 9/10 - It may have a bit false positives.
     * Efficiency: 8/10 - Detects a lot of Scaffold/Tower.
     */
    private void typeC(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            float angle = player.position.getDirection().angle(e.getPlaceBlockFace());
            if (angle > Math.toRadians(config.max_angle)) {
                this.debug("Failed: TypeF, a:" + angle);

                // Punish
                this.punish(player, data, "TypeF", 0);
            }
        }
    }
}