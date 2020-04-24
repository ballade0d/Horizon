package xyz.hstudio.horizon.menu;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import xyz.hstudio.horizon.Horizon;

public class MenuListener implements Listener {

    public MenuListener() {
        Bukkit.getPluginManager().registerEvents(this, Horizon.getInst());
    }

    @EventHandler
    public void onClick(final InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        if (!(inventory.getHolder() instanceof MenuHolder)) {
            return;
        }
        e.setCancelled(true);
        MenuHolder holder = (MenuHolder) inventory.getHolder();
        holder.menu.onClick(e.getSlot(), inventory.getItem(e.getSlot()));
    }

    @EventHandler
    public void onClose(final InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        if (!(inventory.getHolder() instanceof MenuHolder)) {
            return;
        }
        MenuHolder holder = (MenuHolder) inventory.getHolder();
        holder.menu.onClose();
    }
}