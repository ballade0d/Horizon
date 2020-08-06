package xyz.hstudio.horizon.event.outbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class VehicleEvent extends Event {

    public final int vehicle;

    public VehicleEvent(final HoriPlayer player, final int vehicle) {
        super(player);
        this.vehicle = vehicle;
    }

    @Override
    public void post() {
        player.vehicle = vehicle;
        if (vehicle == -1) {
            player.vehicleBypass = true;
            player.sendSimulatedAction(() -> player.vehicleBypass = false);
        }
    }
}