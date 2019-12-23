package xyz.hstudio.horizon.bukkit.network.events.inbound;

import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class InteractItemEvent extends Event {

    public final ItemStack itemStack;
    public final InteractType interactType;

    public InteractItemEvent(final HoriPlayer player, final ItemStack itemStack, final InteractType interactType, final WrappedPacket packet) {
        super(player, packet);
        this.itemStack = itemStack;
        this.interactType = interactType;
    }

    @Override
    public boolean pre() {
        return true;
    }

    @Override
    public void post() {
    }

    public enum InteractType {
        START_USE_ITEM, RELEASE_USE_ITEM, DROP_HELD_ITEM_STACK, DROP_HELD_ITEM
    }
}