package xyz.hstudio.horizon.event.inbound;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class SyncWindowClickEvent extends Event {

    public final InventoryView view;
    public final ClickType click;
    public final InventoryAction action;
    public final Inventory clickedInventory;
    public final InventoryType.SlotType slotType;
    public final int slot;
    public final int rawSlot;
    public final ItemStack current;
    public final int hotbarKey;

    public SyncWindowClickEvent(final HoriPlayer player, final InventoryView view, final InventoryType.SlotType slotType, final int rawSlot, final ClickType click, final InventoryAction action, final int hotbarKey) {
        super(player);
        this.view = view;
        this.current = null;
        this.slotType = slotType;
        this.rawSlot = rawSlot;
        if (rawSlot < 0) {
            this.clickedInventory = null;
        } else if (view.getTopInventory() != null && rawSlot < view.getTopInventory().getSize()) {
            this.clickedInventory = view.getTopInventory();
        } else {
            this.clickedInventory = view.getBottomInventory();
        }

        this.slot = view.convertSlot(rawSlot);
        this.click = click;
        this.action = action;
        this.hotbarKey = hotbarKey;
    }

    public ItemStack getCursor() {
        return this.view.getCursor();
    }

    public ItemStack getCurrentItem() {
        return this.slotType == InventoryType.SlotType.OUTSIDE ? this.current : this.view.getItem(this.rawSlot);
    }
}