package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class CustomPayloadEvent extends Event {

    public final String brand;
    public final int length;

    public CustomPayloadEvent(final HoriPlayer player, final String brand, final int length) {
        super(player);
        this.brand = brand;
        this.length = length;
    }
}