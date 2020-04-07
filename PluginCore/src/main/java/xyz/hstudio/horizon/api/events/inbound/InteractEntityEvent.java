package xyz.hstudio.horizon.api.events.inbound;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.enums.Hand;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class InteractEntityEvent extends Event {

    public final InteractType action;
    public final Vector3D intersection;
    public final Entity entity;
    public final Hand hand;

    public InteractEntityEvent(final HoriPlayer player, final InteractType action, final Vector3D intersection, final Entity entity, final Hand hand) {
        super(player);
        this.action = action;
        this.intersection = intersection;
        this.entity = entity;
        this.hand = hand;
    }

    @Override
    public boolean pre() {
        if (action != InteractType.ATTACK || !(entity instanceof Player)) {
            return true;
        }
        if (McAccessor.INSTANCE.isAccumulated(player.player)) {
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