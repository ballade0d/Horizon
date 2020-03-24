package xyz.hstudio.horizon.api.events.outbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

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
            player.leaveVehicleTick = player.currentTick;
        }
    }
}