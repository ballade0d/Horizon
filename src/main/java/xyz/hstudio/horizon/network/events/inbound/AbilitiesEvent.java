package xyz.hstudio.horizon.network.events.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class AbilitiesEvent extends Event {

    public final boolean flying;

    public AbilitiesEvent(final HoriPlayer player, final boolean flying, final WrappedPacket packet) {
        super(player, packet);
        this.flying = flying;
    }

    @Override
    public void post() {
        if (player.player.getAllowFlight() && this.flying) {
            player.toggleFlyTime = System.currentTimeMillis();
        }
    }
}