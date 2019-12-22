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

    public final InteractAction action;
    public final Entity entity;
    public final Hand hand;

    public InteractEntityEvent(final HoriPlayer player, final InteractAction action, final Entity entity, final Hand hand, final WrappedPacket packet) {
        super(player, packet);
        this.action = action;
        this.entity = entity;
        this.hand = hand;
    }

    @Override
    public boolean pre() {
        if (action != InteractAction.ATTACK || !(entity instanceof Player)) {
            return true;
        }
        if (McAccess.getInst().isAccumulated(player.player)) {
            ItemStack itemStack = this.player.getHeldItem();
            if (this.player.isSprinting || (itemStack != null && itemStack.containsEnchantment(Enchantment.KNOCKBACK))) {
                this.player.hitSlowdownTick = this.player.currentTick;
            }
        }
        return true;
    }

    @Override
    public void post() {
    }

    public enum InteractAction {
        ATTACK, INTERACT
    }
}