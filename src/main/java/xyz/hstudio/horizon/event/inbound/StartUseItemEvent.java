package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;

public class StartUseItemEvent extends Event<PacketPlayInBlockPlace> {

    public final ItemStack itemStack;

    public StartUseItemEvent(HPlayer p, ItemStack itemStack) {
        super(p);
        this.itemStack = itemStack;
    }

    @Override
    public void post() {
        if (itemStack == null || p.bukkit.getGameMode() == GameMode.CREATIVE || p.bukkit.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        Material mat = itemStack.getType();

        // Player can still eat golden apple even if they're full.
        if (mat.isEdible() && (p.bukkit.getFoodLevel() < 20 || mat == Material.GOLDEN_APPLE)) {
            p.status.isEating = true;
        }
        if (mat == Material.BOW && p.bukkit.getInventory().contains(Material.ARROW)) {
            p.status.isPullingBow = true;
        }
        if (EnchantmentTarget.WEAPON.includes(mat)) {
            p.status.isBlocking = true;
        }
    }
}
