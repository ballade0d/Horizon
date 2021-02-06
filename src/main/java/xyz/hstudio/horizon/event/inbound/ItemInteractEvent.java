package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.wrapper.ItemWrapper;

public class ItemInteractEvent extends Event<PacketPlayInBlockDig> {

    public final InteractType type;
    public final ItemWrapper itemStack;

    public ItemInteractEvent(HPlayer p, InteractType type, ItemWrapper itemStack) {
        super(p);
        this.type = type;
        this.itemStack = itemStack;
    }

    @Override
    public void post() {
        p.status.isEating = p.status.isPullingBow = p.status.isBlocking = false;
    }

    public enum InteractType {
        DROP_ALL_ITEMS, DROP_ITEM, RELEASE_USE_ITEM
    }
}