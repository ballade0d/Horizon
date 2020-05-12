package xyz.hstudio.horizon.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public abstract class AbstractMenu {

    protected final MenuHolder holder;

    public abstract void onClick(int slot, ItemStack item);

    public abstract void onOpen();

    public void onClose() {
        holder.hPlayer.prevMenu = this;
    }

    public void open() {
        holder.open();
        this.onOpen();
    }
}