package xyz.hstudio.horizon.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public class AbilitiesEvent extends Event {

    public final boolean flying;

    public AbilitiesEvent(final HoriPlayer player, final boolean flying) {
        super(player);
        this.flying = flying;
    }

    @Override
    public void post() {
        if (player.getPlayer().getAllowFlight() && this.flying) {
            player.toggleFlyTick = player.currentTick;
        }
        if (player.getPlayer().isFlying() && !this.flying) {
            player.toggleFlyTick = player.currentTick;
        }
    }
}