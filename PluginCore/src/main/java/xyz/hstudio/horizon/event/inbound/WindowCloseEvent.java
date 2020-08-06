package xyz.hstudio.horizon.event.inbound;

import org.bukkit.inventory.Inventory;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class WindowCloseEvent extends Event {

    public final Inventory inventory;

    public WindowCloseEvent(final HoriPlayer player, final Inventory inventory) {
        super(player);
        this.inventory = inventory;
    }
}