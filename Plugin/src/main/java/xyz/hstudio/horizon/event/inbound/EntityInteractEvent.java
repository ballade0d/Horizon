package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.EntityBase;

@RequiredArgsConstructor
@Getter
public class EntityInteractEvent extends InEvent {

    private final int entityId;
    private final EntityInteractEvent.InteractType type;
    private final Vector3D cursorPos;
    private final EntityBase entity;

    public enum InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }
}