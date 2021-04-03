package xyz.hstudio.horizon.event.inbound;

import me.cgoo.api.util.IntObjPair;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;

public class BlockInteractEvent extends Event<PacketPlayInBlockPlace> {

    public final Material material;
    public final Vector3D targetPos;
    public final Vector3D cursorPos;
    public final Vector3D placePos;
    public final Direction dir;
    public final InteractType type;

    public BlockInteractEvent(HPlayer p, Material material, Vector3D targetPos, Vector3D cursorPos, Vector3D placePos, Direction dir, InteractType type) {
        super(p);
        this.material = material;
        this.targetPos = targetPos;
        this.cursorPos = cursorPos;
        this.placePos = placePos;
        this.dir = dir;
        this.type = type;
    }

    @Override
    public void post() {
        if (type == InteractType.PLACE_BLOCK) {
            p.clientBlocks.put(placePos, new IntObjPair<>(p.currTick, material));
        }
    }

    public enum InteractType {
        PLACE_BLOCK, INTERACT_BLOCK
    }
}