package xyz.hstudio.horizon.bukkit.network.events.inbound;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.util.Hand;

public class SwingEvent extends Event {

    public final Hand hand;

    public SwingEvent(final HoriPlayer player, final Hand hand, final WrappedPacket packet) {
        super(player, packet);
        this.hand = hand;
    }
}