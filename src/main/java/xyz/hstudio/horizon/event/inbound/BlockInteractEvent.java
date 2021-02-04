package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;

public class BlockInteractEvent extends Event<PacketPlayInBlockPlace> {

    public final Vector3D targetPos;
    public final Vector3D cursorPos;
    public final Vector3D placePos;
    public final Direction dir;
    public final InteractType type;

    public BlockInteractEvent(HPlayer p, Vector3D targetPos, Vector3D cursorPos, Vector3D placePos, Direction dir, InteractType type) {
        super(p);
        this.targetPos = targetPos;
        this.cursorPos = cursorPos;
        this.placePos = placePos;
        this.dir = dir;
        this.type = type;
    }

    public enum InteractType {
        PLACE_BLOCK, INTERACT_BLOCK
    }
}