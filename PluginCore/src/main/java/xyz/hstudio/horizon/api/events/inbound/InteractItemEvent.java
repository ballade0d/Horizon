package xyz.hstudio.horizon.api.events.inbound;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.enums.MatUtils;

public class InteractItemEvent extends Event {

    public final ItemStack itemStack;
    public final Hand hand;
    public final InteractType interactType;

    public InteractItemEvent(final HoriPlayer player, final ItemStack itemStack, final Hand hand, final InteractType interactType) {
        super(player);
        this.itemStack = itemStack;
        this.hand = hand;
        this.interactType = interactType;
    }

    @Override
    public void post() {
        if (this.itemStack == null) {
            return;
        }
        Material mat = this.itemStack.getType();
        if (this.interactType == InteractType.START_USE_ITEM &&
                player.player.getGameMode() != GameMode.CREATIVE &&
                player.player.getGameMode() != GameMode.SPECTATOR) {
            // Player can still eat golden apple even if they're full.
            if (mat.isEdible() && (player.player.getFoodLevel() < 20 || mat == Material.GOLDEN_APPLE || mat == MatUtils.ENCHANTED_GOLDEN_APPLE.parse())) {
                player.isEating = true;
            }
            if (mat == Material.BOW && (player.player.getInventory().contains(Material.ARROW) || player.player.getGameMode() == GameMode.CREATIVE)) {
                player.isPullingBow = true;
            }
            if (MatUtils.BLOCKABLE.contains(mat)) {
                player.isBlocking = true;
            }
        } else if (this.interactType == InteractType.RELEASE_USE_ITEM) {
            player.isEating = false;
            player.isPullingBow = false;
            player.isBlocking = false;
        }
    }

    public enum Hand {
        MAIN, OFF
    }

    public enum InteractType {
        START_USE_ITEM, RELEASE_USE_ITEM, DROP_HELD_ITEM_STACK, DROP_HELD_ITEM
    }
}