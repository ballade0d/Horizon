package xyz.hstudio.horizon.events.inbound;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.util.enums.BlockFace;
import xyz.hstudio.horizon.util.wrap.ClientBlock;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

public class BlockBreakEvent extends Event {

    public final IWrappedBlock block;
    public final BlockFace direction;
    public final ItemStack itemStack;
    public final DigType digType;

    public BlockBreakEvent(final HoriPlayer player, final IWrappedBlock block, final BlockFace direction, final ItemStack itemStack, final DigType digType) {
        super(player);
        this.block = block;
        this.direction = direction;
        this.itemStack = itemStack;
        this.digType = digType;
    }

    public Vector3D getBreakBlockFace() {
        switch (direction) {
            case TOP:
                return new Vector3D(0, -1, 0);
            case BOTTOM:
                return new Vector3D(0, 1, 0);
            case SOUTH:
                return new Vector3D(0, 0, -1);
            case NORTH:
                return new Vector3D(0, 0, 1);
            case WEST:
                return new Vector3D(1, 0, 0);
            case EAST:
                return new Vector3D(-1, 0, 0);
            case INVALID:
            default:
                return new Vector3D(0, 0, 0);
        }
    }

    public Vector3D getTargetBlockFace() {
        switch (direction) {
            case TOP:
                return new Vector3D(0, 1, 0);
            case BOTTOM:
                return new Vector3D(0, -1, 0);
            case SOUTH:
                return new Vector3D(0, 0, 1);
            case NORTH:
                return new Vector3D(0, 0, -1);
            case WEST:
                return new Vector3D(-1, 0, 0);
            case EAST:
                return new Vector3D(1, 0, 0);
            case INVALID:
            default:
                return new Vector3D(0, 0, 0);
        }
    }

    @Override
    public void post() {
        if (this.digType == DigType.COMPLETE) {
            player.addClientBlock(block.getPos(), new ClientBlock(player.currentTick, Material.AIR));
        }
    }

    public enum DigType {
        START, CANCEL, COMPLETE
    }
}