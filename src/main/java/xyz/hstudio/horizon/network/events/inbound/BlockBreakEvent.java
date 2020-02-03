package xyz.hstudio.horizon.network.events.inbound;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.WrappedPacket;
import xyz.hstudio.horizon.util.wrap.ClientBlock;
import xyz.hstudio.horizon.util.wrap.Location;

public class BlockBreakEvent extends Event {

    public final Block block;
    public final BlockFace direction;
    public final ItemStack itemStack;
    public final DigType digType;

    public BlockBreakEvent(final HoriPlayer player, final Block block, final BlockFace direction, final ItemStack itemStack, final DigType digType, final WrappedPacket packet) {
        super(player, packet);
        this.block = block;
        this.direction = direction;
        this.itemStack = itemStack;
        this.digType = digType;
    }

    @Override
    public void post() {
        if (this.digType == DigType.COMPLETE) {
            ClientBlock clientBlock = new ClientBlock(player.currentTick, Material.AIR);
            player.addClientBlock(new Location(this.block.getLocation()), clientBlock);
        }
    }

    public enum DigType {
        START, CANCEL, COMPLETE
    }
}