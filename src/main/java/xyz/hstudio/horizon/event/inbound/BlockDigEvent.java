package xyz.hstudio.horizon.event.inbound;

import me.cgoo.api.util.IntObjPair;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;
import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockWrapper;

public class BlockDigEvent extends Event<PacketPlayInBlockDig> {

    public final Vector3D pos;
    public final Direction dir;
    public final DigType type;
    public final BlockWrapper block;

    public BlockDigEvent(HPlayer p, Vector3D pos, Direction dir, DigType type, BlockWrapper block) {
        super(p);
        this.pos = pos;
        this.dir = dir;
        this.type = type;
        this.block = block;
    }

    @Override
    public void post() {
        if (type == DigType.STOP_DESTROY_BLOCK) {
            p.clientBlocks.put(pos, new IntObjPair<>(p.currTick, Material.AIR));
        }
    }

    public enum DigType {
        START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK
    }
}