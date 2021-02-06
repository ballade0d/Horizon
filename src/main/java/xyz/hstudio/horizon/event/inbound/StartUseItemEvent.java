package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_8_R3.WorldSettings;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.wrapper.ItemWrapper;

public class StartUseItemEvent extends Event<PacketPlayInBlockPlace> {

    public final ItemWrapper itemStack;

    public StartUseItemEvent(HPlayer p, ItemWrapper itemStack) {
        super(p);
        this.itemStack = itemStack;
    }

    @Override
    public void post() {
        if (itemStack == null || p.getMode() == WorldSettings.EnumGamemode.CREATIVE || p.getMode() == WorldSettings.EnumGamemode.SPECTATOR) {
            return;
        }
        Material mat = itemStack.type();

        // Player can still eat golden apple even if they're full.
        if (mat.isEdible() && (p.nms.getFoodData().foodLevel < 20 || mat == Material.GOLDEN_APPLE)) {
            p.status.isEating = true;
        }
        if (mat == Material.BOW && p.inventory.contains(Material.ARROW)) {
            p.status.isPullingBow = true;
        }
        if (EnchantmentTarget.WEAPON.includes(mat)) {
            p.status.isBlocking = true;
        }
    }
}
