package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;

@RequiredArgsConstructor
@Getter
public class BlockInteractEvent extends InEvent {

    private final Vector3D targetPos;
    private final Vector3D cursorPos;
    private final Vector3D placePos;
    private final Direction dir;
    private final InteractType type;

    public enum InteractType {
        PLACE_BLOCK, INTERACT_BLOCK
    }
}