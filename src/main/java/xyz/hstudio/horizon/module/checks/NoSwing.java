package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.api.events.inbound.SwingEvent;
import xyz.hstudio.horizon.config.checks.NoSwingConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.NoSwingData;
import xyz.hstudio.horizon.module.Module;

public class NoSwing extends Module<NoSwingData, NoSwingConfig> {

    public NoSwing() {
        super(ModuleType.NoSwing, new NoSwingConfig());
    }

    @Override
    public NoSwingData getData(final HoriPlayer player) {
        return player.noSwingData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final NoSwingData data, final NoSwingConfig config) {
        event.setCancelled(true);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final NoSwingData data, final NoSwingConfig config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    private void typeA(final Event event, final HoriPlayer player, final NoSwingData data, final NoSwingConfig config) {
        if (event instanceof SwingEvent) {
            data.animationExpected = false;
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            if (data.animationExpected) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(event, player, data, "TypeA", 5);
            } else {
                reward("TypeA", data, 0.999);
            }
            data.animationExpected = true;
        }
    }
}