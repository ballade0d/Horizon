package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class EntityActionEvent extends InEvent {

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
                break;
            case STOP_SPRINTING:
                p.status.isSprinting = false;
                break;
        }
        super.post();
    }

    public enum ActionType {
        PERFORM_RESPAWN, REQUEST_STATS, OPEN_INVENTORY, START_SNEAKING, STOP_SNEAKING, STOP_SLEEPING, START_SPRINTING, STOP_SPRINTING, RIDING_JUMP
    }
}