package xyz.hstudio.horizon.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public abstract class IMenu {

    protected final MenuHolder holder;

    public abstract void onClick(int slot, ItemStack item);

    public abstract void onClose();

    public abstract void open();
}