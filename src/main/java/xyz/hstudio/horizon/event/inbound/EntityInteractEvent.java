package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.EntityBase;

public class EntityInteractEvent extends InEvent<PacketPlayInUseEntity> {

    public final int entityId;
    public final InteractType type;
    public final Vector3D cursorPos;
    public final EntityBase entity;

    public EntityInteractEvent(HPlayer p, int entityId, InteractType type, Vector3D cursorPos, EntityBase entity) {
        super(p);
        this.entityId = entityId;
        this.type = type;
        this.cursorPos = cursorPos;
        this.entity = entity;
    }

    @Override
    public void post() {
        if (type != InteractType.ATTACK || !(entity instanceof Player)) {
            return;
        }
        ItemStack itemStack = p.inventory.mainHand();
        if (p.status.isSprinting || (itemStack != null && itemStack.containsEnchantment(Enchantment.KNOCKBACK))) {
            p.status.hitSlowdown = true;
        }
    }

    public enum InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }
}