package xyz.hstudio.horizon.event.inbound;

import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class ItemInteractEvent extends InEvent {

    public final InteractType type;
    public final ItemStack itemStack;

    public ItemInteractEvent(HPlayer p, InteractType type, ItemStack itemStack) {
        super(p);
        this.type = type;
        this.itemStack = itemStack;
    }

    public enum InteractType {
        DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, START_USE_ITEM
    }
}