package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.BlockBreakEvent;
import xyz.hstudio.horizon.api.events.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.api.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.api.events.inbound.SwingEvent;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.NoSwingData;
import xyz.hstudio.horizon.file.node.NoSwingNode;
import xyz.hstudio.horizon.module.Module;

public class NoSwing extends Module<NoSwingData, NoSwingNode> {

    public NoSwing() {
        super(ModuleType.NoSwing, new NoSwingNode(), "TypeA", "TypeB");
    }

    @Override
    public NoSwingData getData(final HoriPlayer player) {
        return player.noSwingData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final NoSwingData data, final NoSwingNode config) {
        event.setCancelled(true);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final NoSwingData data, final NoSwingNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
        if (config.typeB_enabled) {
            typeB(event, player, data, config);
        }
    }

    private void typeA(final Event event, final HoriPlayer player, final NoSwingData data, final NoSwingNode config) {
        if (event instanceof SwingEvent) {
            data.animationExpected = false;
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            if (data.animationExpected) {
                // Punish
                this.punish(event, player, data, 0, 5);
            } else {
                reward(0, data, 0.999);
            }
            data.animationExpected = true;
        }
    }

    // This check has rare false positives; more testing is required.
    private void typeB(final Event event, final HoriPlayer player, final NoSwingData data, final NoSwingNode config) {
        if (event instanceof SwingEvent) {
            data.animationExpected = false;
        } else if (event instanceof BlockBreakEvent) {
            BlockBreakEvent e = (BlockBreakEvent) event;
            if (e.digType != BlockBreakEvent.DigType.START) {
                return;
            }
            if (data.animationExpected) {
                this.punish(event, player, data, 1, 5);
            } else {
                reward(1, data, 0.999);
            }
            data.animationExpected = true;
        } else if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            if (data.animationExpected) {
                this.punish(event, player, data, 1, 5);
            } else {
                reward(1, data, 0.999);
            }
            data.animationExpected = true;
        }
    }
}