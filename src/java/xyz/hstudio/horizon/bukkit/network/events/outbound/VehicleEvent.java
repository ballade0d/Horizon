package xyz.hstudio.horizon.bukkit.network.events.outbound;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class VehicleEvent extends Event {

    public final int vehicle;

    public VehicleEvent(final HoriPlayer player, final int vehicle, final WrappedPacket packet) {
        super(player, packet);
        this.vehicle = vehicle;
    }

    @Override
    public boolean pre() {
        return true;
    }

    @Override
    public void post() {
        player.vehicle = vehicle;
    }
}