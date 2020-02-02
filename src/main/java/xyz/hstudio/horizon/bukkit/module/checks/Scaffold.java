package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.block.Block;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.ScaffoldConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.ScaffoldData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.Vector3D;

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
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
        if (config.typeB_enabled) {
            typeB(event, player, data, config);
        }
        if (config.typeC_enabled) {
            typeC(event, player, data, config);
        }
        if (config.typeD_enabled) {
            typeD(event, player, data, config);
        }
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

            Vector3D interaction = e.interaction;
            Block b = e.getTargetLocation().getBlock();
            if (e.face == BlockPlaceEvent.BlockFace.INVALID) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 5);
            } else if (DoubleStream.of(interaction.x, interaction.y, interaction.z)
                    .anyMatch(d -> d > 1 || d < 0)) {
                this.debug("Failed: TypeA, i:" + interaction.toString());

                // Punish
                this.punish(player, data, "TypeA", 5);
            } else if (b != null && b.getType().name().contains("AIR")) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 5);
            } else {
                reward("TypeA", data, 0.999);
            }
        }
    }

    /**
     * An easy place angle check.
     * <p>
     * Accuracy: 9/10 - It may have a bit false positives.
     * Efficiency: 6/10 - Detects some poorly made Scaffold/Tower.
     *
     * @author MrCraftGoo
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

    /**
     * A place packet order check.
     * <p>
     * Accuracy: 8/10 - Should not have much false positives
     * Efficiency: 9/10 - Detects post scaffold almost instantly
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }

            long deltaT = System.currentTimeMillis() - data.lastMove;
            if (data.lagging || deltaT >= 20) {
                if (data.typeCFails > 0) {
                    data.typeCFails--;
                } else {
                    reward("TypeC", data, 0.999);
                }
                return;
            }
            if (++data.typeCFails > 4) {
                this.debug("Failed: TypeC, d:" + deltaT);

                // Punish
                this.punish(player, data, "TypeC", 4);
            }
        } else if (event instanceof MoveEvent) {
            long now = System.currentTimeMillis();
            data.lagging = now - data.lastMove < 5;
            data.lastMove = now;
        }
    }

    /**
     * A direction check. It can detect a large amount of scaffold.
     * <p>
     * Accuracy: 7/10 - It may have some false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeD(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (player.currentTick - data.lastPlaceTick > 6 || e.isTeleport) {
                return;
            }
            // TODO: Add a threshold
            if (!e.strafeNormally) {
                this.debug("Failed: TypeD");

                // Punish
                this.punish(player, data, "TypeD", 4);
            } else {
                reward("TypeD", data, 0.999);
            }
        } else if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            data.lastPlaceTick = player.currentTick;
        }
    }
}