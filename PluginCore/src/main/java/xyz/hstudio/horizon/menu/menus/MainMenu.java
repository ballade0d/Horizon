package xyz.hstudio.horizon.menu.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.hstudio.horizon.menu.AbstractMenu;
import xyz.hstudio.horizon.menu.MenuHolder;

import java.util.HashMap;
import java.util.Map;

public class MainMenu extends AbstractMenu {

    private static final Map<Integer, ItemStack> ITEMS;

    static {
        ITEMS = new HashMap<>();
        ItemMeta meta;

        ItemStack settings = new ItemStack(Material.COMMAND);
        meta = settings.getItemMeta();
        meta.setDisplayName("§aSettings");
        //
        settings.setItemMeta(meta);

        ITEMS.put(10, settings);

        ItemStack statistics = new ItemStack(Material.SIGN);
        meta = statistics.getItemMeta();
        meta.setDisplayName("§aStatistics");
        //
        statistics.setItemMeta(meta);

        ITEMS.put(13, statistics);

        ItemStack reload = new ItemStack(Material.EMERALD_BLOCK);
        meta = reload.getItemMeta();
        meta.setDisplayName("§aReload");
        //
        reload.setItemMeta(meta);

        ITEMS.put(16, reload);
    }

    public MainMenu(final MenuHolder holder) {
        super(holder);
        Inventory inventory = Bukkit.createInventory(holder, 27, "§9§lHorizon");

        for (Map.Entry<Integer, ItemStack> entry : ITEMS.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }

        holder.inventory = inventory;
        holder.menu = this;
    }

    @Override
    public void onClick(final int slot, final ItemStack item) {
        if (slot == 10 && item.equals(ITEMS.get(10))) {
            // TODO
        } else if (slot == 13 && item.equals(ITEMS.get(13))) {
            // TODO
        } else if (slot == 16 && item.equals(ITEMS.get(16))) {
            // TODO
        }
    }

    @Override
    public void onOpen() {
    }
}