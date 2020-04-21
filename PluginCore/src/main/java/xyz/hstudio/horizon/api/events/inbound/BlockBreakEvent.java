package xyz.hstudio.horizon.api.events.inbound;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;
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

    @Override
    public void post() {
        if (this.digType == DigType.COMPLETE) {
            player.addClientBlock(this.block.getPos(), player.currentTick, Material.AIR);
        }
    }

    public enum DigType {
        START, CANCEL, COMPLETE
    }
}