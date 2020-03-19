package xyz.hstudio.horizon.api.events.inbound;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.wrap.Location;

public class BlockBreakEvent extends Event {

    public final Block block;
    public final BlockFace direction;
    public final ItemStack itemStack;
    public final DigType digType;

    public BlockBreakEvent(final HoriPlayer player, final Block block, final BlockFace direction, final ItemStack itemStack, final DigType digType) {
        super(player);
        this.block = block;
        this.direction = direction;
        this.itemStack = itemStack;
        this.digType = digType;
    }

    @Override
    public void post() {
        if (this.digType == DigType.COMPLETE) {
            player.addClientBlock(new Location(this.block.getLocation()), player.currentTick);
        }
    }

    public enum DigType {
        START, CANCEL, COMPLETE
    }
}