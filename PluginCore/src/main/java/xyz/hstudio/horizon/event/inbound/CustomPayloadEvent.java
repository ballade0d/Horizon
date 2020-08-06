package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class CustomPayloadEvent extends Event {

    public final String brand;
    public final int length;

    public CustomPayloadEvent(final HoriPlayer player, final String brand, final int length) {
        super(player);
        this.brand = brand;
        this.length = length;
    }
}