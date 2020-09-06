package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockBase;

@RequiredArgsConstructor
@Getter
public class BlockDigEvent extends InEvent {

    private final Vector3D pos;
    private final Direction dir;
    private final DigType type;
    private final BlockBase block;

    public enum DigType {
        START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK
    }
}