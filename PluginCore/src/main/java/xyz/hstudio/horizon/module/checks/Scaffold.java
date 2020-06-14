package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.ScaffoldData;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.events.inbound.BlockBreakEvent;
import xyz.hstudio.horizon.events.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.events.inbound.MoveEvent;
import xyz.hstudio.horizon.file.node.ScaffoldNode;
import xyz.hstudio.horizon.module.Module;

public class Scaffold extends Module<ScaffoldData, ScaffoldNode> {

    public Scaffold() {
        super(ModuleType.Scaffold, new ScaffoldNode(), "Order", "Direction");
    }

    @Override
    public ScaffoldData getData(final HoriPlayer player) {
        return player.scaffoldData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
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

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
        if (config.order_enabled) {
            typeA(event, player, data, config);
        }
        if (config.direction_enabled) {
            typeB(event, player, data, config);
        }
        // TODO: More checks
    }

    /**
     * A place packet order check.
     * <p>
     * Accuracy: 10/10 - Haven't found any false positives.
     * Efficiency: 9/10 - Detects post scaffold instantly
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }

            long deltaT = System.currentTimeMillis() - data.lastMove;
            if (data.lagging || deltaT >= 20) {
                if (data.typeAFails > 0) {
                    data.typeAFails--;
                } else {
                    reward(2, data, 0.999);
                }
                return;
            }
            if (++data.typeAFails > 4) {
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
     * Accuracy: 9/10 - It may have some false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final ScaffoldData data, final ScaffoldNode config) {
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
                if (++data.typeBFails > 2) {
                    // Punish
                    this.punish(event, player, data, 3, 3);
                }
            } else if (data.typeBFails > 0) {
                data.typeBFails--;
            } else {
                reward(3, data, 0.995);
            }
        }
    }
}