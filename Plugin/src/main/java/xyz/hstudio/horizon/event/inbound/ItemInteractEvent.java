package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.enums.Hand;

@RequiredArgsConstructor
@Getter
public class ItemInteractEvent extends InEvent {

    private final InteractType type;
    private final ItemStack itemStack;
    private final Hand hand;

    public enum InteractType {
        DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM, START_USE_ITEM
    }
}