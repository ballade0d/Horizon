package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class AbilitiesEvent extends Event {

    public final boolean flying;

    public AbilitiesEvent(final HoriPlayer player, final boolean flying) {
        super(player);
        this.flying = flying;
    }

    @Override
    public void post() {
        if (player.player.getAllowFlight() && this.flying) {
            player.toggleFlyTick = player.currentTick;
        }
        if (player.player.isFlying() && !this.flying) {
            player.toggleFlyTick = player.currentTick;
        }
    }
}