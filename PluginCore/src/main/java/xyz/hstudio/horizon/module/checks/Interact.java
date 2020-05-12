package xyz.hstudio.horizon.module.checks;

import org.bukkit.GameMode;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.BlockBreakEvent;
import xyz.hstudio.horizon.api.events.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.InteractData;
import xyz.hstudio.horizon.file.node.InteractNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.enums.BlockFace;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.stream.DoubleStream;

public class Interact extends Module<InteractData, InteractNode> {

    public Interact() {
        super(ModuleType.Interact, new InteractNode(), "Packet", "Angle", "Order", "Direction");
    }

    @Override
    public InteractData getData(final HoriPlayer player) {
        return player.interactData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (type == 0 || type == 1 || type == 2 || type == 3) {
            if (event instanceof BlockBreakEvent) {
                BlockBreakEvent e = (BlockBreakEvent) event;
                e.setCancelled(true);
                McAccessor.INSTANCE.updateBlock(player, e.block.getPos());
            } else if (event instanceof BlockPlaceEvent) {
                BlockPlaceEvent e = (BlockPlaceEvent) event;
                e.setCancelled(true);
                McAccessor.INSTANCE.updateBlock(player, e.placed);
            }
        }
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (config.packet_enabled) {
            typeA(event, player, data, config);
        }
        if (config.angle_enabled) {
            typeB(event, player, data, config);
        }
        if (config.order_enabled) {
            typeC(event, player, data, config);
        }
        if (config.direction_enabled) {
            typeD(event, player, data, config);
        }
    }

    /**
     * An easy packet check. This will detect some poorly made Scaffold/Tower.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positive.
     * Efficiency: 8/10 - Detects some Block hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            if (player.getPlayer().getGameMode() == GameMode.CREATIVE) {
                return;
            }

            Vector3D interaction = e.interaction;
            if (e.face == BlockFace.INVALID) {
                // Punish
                this.punish(event, player, data, 0, 5, "p:1");
            } else if (DoubleStream.of(interaction.x, interaction.y, interaction.z)
                    .anyMatch(d -> d > 1 || d < 0)) {
                // Punish
                this.punish(event, player, data, 0, 5, "1:" + interaction);
            } else {
                reward(0, data, 0.999);
            }
        }
    }

    /**
     * An angle check.
     * <p>
     * Accuracy: 9/10 - It may have a bit false positives.
     * Efficiency: 7/10 - Detects some poorly made Scaffold/Tower.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            float angle = player.position.getDirection().angle(e.getPlaceBlockFace());
            if (angle > Math.toRadians(config.angle_max_angle)) {
                // Punish
                this.punish(event, player, data, 1, 4, "t:place");
            } else {
                reward(1, data, 0.999);
            }
        } else if (event instanceof BlockBreakEvent) {
            BlockBreakEvent e = (BlockBreakEvent) event;
            if (e.digType != BlockBreakEvent.DigType.COMPLETE) {
                return;
            }
            float angle = player.position.getDirection().angle(e.getBreakBlockFace());
            if (angle > Math.toRadians(config.angle_max_angle)) {
                // Punish
                this.punish(event, player, data, 1, 4, "t:break");
            } else {
                reward(1, data, 0.999);
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
    private void typeC(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
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
                    reward(2, data, 0.999);
                }
                return;
            }
            if (++data.typeCFails > 4) {
                // Punish
                this.punish(event, player, data, 2, 4);
            }
        } else if (event instanceof MoveEvent) {
            long now = System.currentTimeMillis();
            data.lagging = now - data.lastMove < 5;
            data.lastMove = now;
        }
    }

    /**
     * A direction check. It can detect a large amount of Scaffold.
     * <p>
     * Accuracy: 8/10 - It may have some false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeD(final Event event, final HoriPlayer player, final InteractData data, final InteractNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.isTeleport) {
                return;
            }
            if (!e.strafeNormally) {
                data.lastStrafeTick = player.currentTick;
            }
        } else if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            if (player.currentTick - data.lastStrafeTick <= 3) {
                if (++data.directionFails > 2) {
                    // Punish
                    this.punish(event, player, data, 3, 3);
                }
            } else if (data.directionFails > 0) {
                data.directionFails--;
            } else {
                reward(3, data, 0.999);
            }
        }
    }
}