package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.ScaffoldData;
import xyz.hstudio.horizon.file.node.ScaffoldNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.stream.DoubleStream;

public class Scaffold extends Module<ScaffoldData, ScaffoldNode> {

    public Scaffold() {
        super(ModuleType.Scaffold, new ScaffoldNode());
    }

    @Override
    public ScaffoldData getData(final HoriPlayer player) {
        return player.scaffoldData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
        // TODO: Finish this
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
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
    private void typeA(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }

            Vector3D interaction = e.interaction;
            Block b = e.getTargetLocation().getBlock();
            if (e.face == BlockPlaceEvent.BlockFace.INVALID) {
                // Punish
                this.punish(event, player, data, "TypeA", 5, "p:1");
            } else if (DoubleStream.of(interaction.x, interaction.y, interaction.z)
                    .anyMatch(d -> d > 1 || d < 0)) {
                // Punish
                this.punish(event, player, data, "TypeA", 5, "1:" + interaction);
            } else if (b != null && b.getType() == Material.AIR) {
                // Punish
                this.punish(event, player, data, "TypeA", 5, "p:2");
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
    private void typeB(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            float angle = player.position.getDirection().angle(e.getPlaceBlockFace());
            if (angle > Math.toRadians(config.typeB_max_angle)) {
                // Punish
                this.punish(event, player, data, "TypeB", 4);
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
    private void typeC(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
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
                // Punish
                this.punish(event, player, data, "TypeC", 4);
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
    private void typeD(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (player.currentTick - data.lastPlaceTick > 6 || e.isTeleport) {
                return;
            }
            if (!e.strafeNormally) {
                if (++data.typeDFails > 4) {
                    // Punish
                    this.punish(event, player, data, "TypeD", 3);
                }
            } else if (data.typeDFails > 0) {
                data.typeDFails--;
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