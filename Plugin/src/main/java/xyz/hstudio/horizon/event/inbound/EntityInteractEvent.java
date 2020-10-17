package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.EntityBase;

public class EntityInteractEvent extends InEvent {

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

    public enum InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }
}