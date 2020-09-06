package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.event.InEvent;

@RequiredArgsConstructor
@Getter
public class EntityActionEvent extends InEvent {

    private final ActionType type;
    private final int jumpBoost;

    public enum ActionType {
        PERFORM_RESPAWN, REQUEST_STATS, OPEN_INVENTORY, START_SNEAKING, STOP_SNEAKING, STOP_SLEEPING, START_SPRINTING, STOP_SPRINTING, RIDING_JUMP
    }
}