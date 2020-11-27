package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInEntityAction;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.outbound.AttributeEvent;

public class EntityActionEvent extends InEvent<PacketPlayInEntityAction> {

    public final ActionType type;
    public final int jumpBoost;

    public EntityActionEvent(HPlayer p, ActionType type, int jumpBoost) {
        super(p);
        this.type = type;
        this.jumpBoost = jumpBoost;
    }

    @Override
    public void post() {
        switch (type) {
            case START_SNEAKING:
                p.status.isSneaking = true;
                break;
            case STOP_SNEAKING:
                p.status.isSneaking = false;
                break;
            case START_SPRINTING:
                p.status.isSprinting = true;
                // Client increases moving speed whenever it starts sprinting
                // although the attribute packet haven't been sent back to the client
                // so let's update it here
                p.moveFactors.add(AttributeEvent.SPRINT_MODIFIER);
                break;
            case STOP_SPRINTING:
                p.status.isSprinting = false;
                break;
        }
        super.post();
    }

    public enum ActionType {
        START_SNEAKING, STOP_SNEAKING, STOP_SLEEPING, START_SPRINTING, STOP_SPRINTING, RIDING_JUMP
    }
}