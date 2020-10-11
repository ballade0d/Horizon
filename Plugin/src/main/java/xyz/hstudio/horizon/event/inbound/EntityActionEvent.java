package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

@RequiredArgsConstructor
@Getter
public class EntityActionEvent extends InEvent {

    private final ActionType type;
    private final int jumpBoost;

    @Override
    public void post(HPlayer p) {
        switch (type) {
            case START_SNEAKING:
                p.status().isSneaking = true;
                break;
            case STOP_SNEAKING:
                p.status().isSneaking = false;
                break;
        }
        super.post(p);
    }

    public enum ActionType {
        PERFORM_RESPAWN, REQUEST_STATS, OPEN_INVENTORY, START_SNEAKING, STOP_SNEAKING, STOP_SLEEPING, START_SPRINTING, STOP_SPRINTING, RIDING_JUMP
    }
}