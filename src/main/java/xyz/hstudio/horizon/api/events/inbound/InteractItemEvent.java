package xyz.hstudio.horizon.api.events.inbound;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class InteractItemEvent extends Event {

    public final ItemStack itemStack;
    public final InteractType interactType;

    public InteractItemEvent(final HoriPlayer player, final ItemStack itemStack, final InteractType interactType) {
        super(player);
        this.itemStack = itemStack;
        this.interactType = interactType;
    }

    @Override
    public void post() {
        if (this.itemStack == null) {
            return;
        }
        Material mat = this.itemStack.getType();
        if (this.interactType == InteractType.START_USE_ITEM) {
            // Player can still eat golden apple even if they're full.
            // In 1.13+ there's ENCHANTED_GOLDEN_APPLE so I use String#contains.
            if (mat.isEdible() && (player.player.getFoodLevel() < 20 || mat.name().contains("GOLDEN_APPLE"))) {
                player.isEating = true;
            }
            if (mat == Material.BOW && (player.player.getInventory().contains(Material.ARROW) || player.player.getGameMode() == GameMode.CREATIVE)) {
                player.isPullingBow = true;
            }
        } else if (this.interactType == InteractType.RELEASE_USE_ITEM) {
            player.isEating = false;
            player.isPullingBow = false;
        }
    }

    public enum InteractType {
        START_USE_ITEM, RELEASE_USE_ITEM, DROP_HELD_ITEM_STACK, DROP_HELD_ITEM
    }
}