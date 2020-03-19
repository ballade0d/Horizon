package xyz.hstudio.horizon.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuHolder implements InventoryHolder {

    private final Menu menu;
    private Inventory inventory;

    public MenuHolder(final Menu menu) {
        this.menu = menu;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}