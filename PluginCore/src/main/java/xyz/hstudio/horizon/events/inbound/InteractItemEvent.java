package xyz.hstudio.horizon.events.inbound;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.util.enums.MatUtils;

public class InteractItemEvent extends Event {

    public final ItemStack itemStack;
    public final Hand hand;
    public final InteractType interactType;

    public final boolean useItem;

    public InteractItemEvent(final HoriPlayer player, final ItemStack itemStack, final Hand hand, final InteractType interactType) {
        super(player);
        this.itemStack = itemStack;
        this.hand = hand;
        this.interactType = interactType;

        this.useItem = checkUseItem();
    }

    private boolean checkUseItem() {
        if (this.itemStack == null) {
            return false;
        }
        Player bPlayer = player.getPlayer();
        Material mat = this.itemStack.getType();
        if (this.interactType == InteractType.START_USE_ITEM &&
                bPlayer.getGameMode() != GameMode.CREATIVE &&
                bPlayer.getGameMode() != GameMode.SPECTATOR) {
            // Player can still eat golden apple even if they're full.
            if (mat.isEdible() && (bPlayer.getFoodLevel() < 20 || mat == Material.GOLDEN_APPLE || mat == MatUtils.ENCHANTED_GOLDEN_APPLE.parse())) {
                player.isEating = true;
                return true;
            }
            if (mat == Material.BOW && bPlayer.getInventory().contains(Material.ARROW)) {
                player.isPullingBow = true;
                return true;
            }
            if (MatUtils.BLOCKABLE.contains(mat)) {
                player.isBlocking = true;
                return true;
            }
        } else {
            player.isEating = false;
            player.isPullingBow = false;
            player.isBlocking = false;
        }
        return false;
    }

    public enum Hand {
        MAIN, OFF
    }

    public enum InteractType {
        START_USE_ITEM, RELEASE_USE_ITEM, DROP_HELD_ITEM_STACK, DROP_HELD_ITEM
    }
}