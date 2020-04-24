package xyz.hstudio.horizon.menu;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import xyz.hstudio.horizon.data.HoriPlayer;

@RequiredArgsConstructor
public class MenuHolder implements InventoryHolder {

    public final Player player;
    public final HoriPlayer hPlayer;
    public Inventory inventory;
    public IMenu menu;

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}