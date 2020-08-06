package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.NoSwingData;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.BlockBreakEvent;
import xyz.hstudio.horizon.event.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.event.inbound.SwingEvent;
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
            data.hitSwingExpected = false;
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            if (data.hitSwingExpected) {
                // Punish
                this.punish(event, player, data, 0, 5);
            } else {
                reward(0, data, 0.999);
            }
            data.hitSwingExpected = true;
        }
    }

    private void typeB(final Event event, final HoriPlayer player, final NoSwingData data, final NoSwingNode config) {
        if (event instanceof SwingEvent) {
            data.interactSwingExpected = false;
        } else if (event instanceof BlockBreakEvent) {
            BlockBreakEvent e = (BlockBreakEvent) event;
            if (e.digType != BlockBreakEvent.DigType.COMPLETE) {
                return;
            }
            if (data.interactSwingExpected) {
                this.punish(event, player, data, 1, 5);
            } else {
                reward(1, data, 0.999);
            }
            data.interactSwingExpected = true;
        }
    }
}