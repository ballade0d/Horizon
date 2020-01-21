package xyz.hstudio.horizon.bukkit.network.events.inbound;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.util.Hand;

public class InteractEntityEvent extends Event {

    public final InteractType action;
    public final Entity entity;
    public final Hand hand;

    public InteractEntityEvent(final HoriPlayer player, final InteractType action, final Entity entity, final Hand hand, final WrappedPacket packet) {
        super(player, packet);
        this.action = action;
        this.entity = entity;
        this.hand = hand;
    }

    @Override
    public boolean pre() {
        if (action != InteractType.ATTACK || !(entity instanceof Player)) {
            return true;
        }
        if (McAccess.getInst().isAccumulated(player.player)) {
            ItemStack itemStack = player.getHeldItem();
            if (player.isSprinting || (itemStack != null && itemStack.containsEnchantment(Enchantment.KNOCKBACK))) {
                player.hitSlowdownTick = player.currentTick;
            }
        }
        return true;
    }

    public enum InteractType {
        ATTACK, INTERACT
    }
}