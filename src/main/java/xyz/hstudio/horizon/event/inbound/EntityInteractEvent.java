package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.Enchantment;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.EntityWrapper;
import xyz.hstudio.horizon.wrapper.ItemWrapper;

public class EntityInteractEvent extends Event<PacketPlayInUseEntity> {

    public final int entityId;
    public final InteractType type;
    public final Vector3D cursorPos;
    public final EntityWrapper entity;

    public EntityInteractEvent(HPlayer p, int entityId, InteractType type, Vector3D cursorPos, EntityWrapper entity) {
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
        ItemWrapper itemStack = p.inventory.hand();
        if (p.status.isSprinting || (itemStack != null && itemStack.hasEnchantment(Enchantment.KNOCKBACK))) {
            p.status.hitSlowdown = true;
        }
    }

    public enum InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }
}