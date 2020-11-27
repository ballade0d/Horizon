package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class StartUseItemEvent extends InEvent<PacketPlayInBlockPlace> {

    public final ItemStack itemStack;

    public StartUseItemEvent(HPlayer p, ItemStack itemStack) {
        super(p);
        this.itemStack = itemStack;
    }
}
