package xyz.hstudio.horizon.menu.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.horizon.menu.IMenu;
import xyz.hstudio.horizon.menu.MenuHolder;

public class MainMenu extends IMenu {

    private static final ItemStack SETTINGS_ITEM;
    private static final ItemStack STATISTICS_ITEM;
    private static final ItemStack RELOAD_ITEM;

    static {
        ItemMeta meta;

        SETTINGS_ITEM = new ItemStack(Material.COMMAND);
        meta = SETTINGS_ITEM.getItemMeta();
        meta.setDisplayName("§aSettings");
        //
        SETTINGS_ITEM.setItemMeta(meta);

        STATISTICS_ITEM = new ItemStack(Material.SIGN);
        meta = STATISTICS_ITEM.getItemMeta();
        meta.setDisplayName("§aStatistics");
        //
        STATISTICS_ITEM.setItemMeta(meta);

        RELOAD_ITEM = new ItemStack(Material.EMERALD_BLOCK);
        meta = RELOAD_ITEM.getItemMeta();
        meta.setDisplayName("§aReload");
        //
        RELOAD_ITEM.setItemMeta(meta);
    }

    public MainMenu(final MenuHolder holder) {
        super(holder);
        Inventory inventory = Bukkit.createInventory(holder, 27, "§9§lHorizon");

        inventory.setItem(10, SETTINGS_ITEM);
        inventory.setItem(13, STATISTICS_ITEM);
        inventory.setItem(16, RELOAD_ITEM);

        holder.inventory = inventory;
        holder.menu = this;
    }

    @Override
    public void onClick(final int slot, final ItemStack item) {
        if (slot == 10 && item.equals(SETTINGS_ITEM)) {
            // TODO
        } else if (slot == 13 && item.equals(STATISTICS_ITEM)) {
            // TODO
        } else if (slot == 16 && item.equals(RELOAD_ITEM)) {
            // TODO
        }
    }

    @Override
    public void onClose() {
        holder.hPlayer.prevMenu = this;
    }

    @Override
    public void open() {
        holder.open();
    }
}