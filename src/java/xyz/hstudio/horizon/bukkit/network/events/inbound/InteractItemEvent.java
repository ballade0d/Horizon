package xyz.hstudio.horizon.bukkit.network.events.inbound;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

import static xyz.hstudio.horizon.bukkit.network.events.inbound.InteractItemEvent.InteractType.RELEASE_USE_ITEM;
import static xyz.hstudio.horizon.bukkit.network.events.inbound.InteractItemEvent.InteractType.START_USE_ITEM;

public class InteractItemEvent extends Event {

    public final ItemStack itemStack;
    public final InteractType interactType;

    public InteractItemEvent(final HoriPlayer player, final ItemStack itemStack, final InteractType interactType, final WrappedPacket packet) {
        super(player, packet);
        this.itemStack = itemStack;
        this.interactType = interactType;
    }

    @Override
    public void post() {
        if (this.itemStack == null) {
            return;
        }
        Material mat = this.itemStack.getType();
        if (this.interactType == START_USE_ITEM) {
            // Player can still eat golden apple even if they're full.
            // In 1.13+ there's ENCHANTED_GOLDEN_APPLE so I use String#contains.
            if (mat.isEdible() && (player.player.getFoodLevel() < 20 || mat.name().contains("GOLDEN_APPLE"))) {
                player.isEating = true;
            }
            if (mat == Material.BOW && (player.player.getInventory().contains(Material.ARROW) || player.player.getGameMode() == GameMode.CREATIVE)) {
                player.isPullingBow = true;
            }
        } else if (this.interactType == RELEASE_USE_ITEM) {
            player.isEating = false;
            player.isPullingBow = false;
        }
    }

    public enum InteractType {
        START_USE_ITEM, RELEASE_USE_ITEM, DROP_HELD_ITEM_STACK, DROP_HELD_ITEM
    }
}