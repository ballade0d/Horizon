package xyz.hstudio.horizon.network.events.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;

public class VehicleEvent extends Event {

    public final int vehicle;

    public VehicleEvent(final HoriPlayer player, final int vehicle, final WrappedPacket packet) {
        super(player, packet);
        this.vehicle = vehicle;
    }

    @Override
    public void post() {
        player.vehicle = vehicle;
    }
}