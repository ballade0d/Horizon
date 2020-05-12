package xyz.hstudio.horizon.menu.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.hstudio.horizon.Horizon;
import xyz.hstudio.horizon.menu.AbstractMenu;
import xyz.hstudio.horizon.menu.MenuHolder;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.ItemStackBuilder;

public class SettingsMenu extends AbstractMenu {

    private static final int[] SLOTS = new int[]{};

    private BukkitTask task;

    public SettingsMenu(MenuHolder holder) {
        super(holder);
        Inventory inventory = Bukkit.createInventory(holder, 27, "§9§lHorizon");


        holder.inventory = inventory;
        holder.menu = this;
    }

    @Override
    public void onClick(final int slot, final ItemStack item) {
    }

    @Override
    public void onOpen() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(Horizon.getInst(), () -> {

        }, 2L, 2L);
    }

    @Override
    public void onClose() {
        if (task != null) {
            task.cancel();
        }
        super.onClose();
    }

    private ItemStack getItem(final Module<?, ?> module) {
        return new ItemStackBuilder(Material.SIGN)
                .withLore("§eState: " + (module.getConfig().enabled ? "§aEnabled" : "§cDisabled"))
                .build();
    }
}
