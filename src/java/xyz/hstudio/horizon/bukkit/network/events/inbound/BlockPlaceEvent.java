package xyz.hstudio.horizon.bukkit.network.events.inbound;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class BlockPlaceEvent extends Event {

    @Getter
    private final Location placed;
    @Getter
    private final BlockFace face;
    @Getter
    private final Material material;
    @Getter
    private final Vector interaction;
    @Getter
    private final PlaceType placeType;

    public BlockPlaceEvent(final HoriPlayer player, final Location placed, final BlockFace face, final Material material, final Vector interaction, final PlaceType placeType, final WrappedPacket packet) {
        super(player, packet);
        this.placed = placed;
        this.face = face;
        this.material = material;
        this.interaction = interaction;
        this.placeType = placeType;
    }

    public Location getTargetLocation() {
        switch (face) {
            case TOP:
                return new Location(placed.getWorld(), placed.getX(), placed.getY() - 1, placed.getZ());
            case EAST:
                return new Location(placed.getWorld(), placed.getX() - 1, placed.getY(), placed.getZ());
            case WEST:
                return new Location(placed.getWorld(), placed.getX() + 1, placed.getY(), placed.getZ());
            case NORTH:
                return new Location(placed.getWorld(), placed.getX(), placed.getY(), placed.getZ() + 1);
            case SOUTH:
                return new Location(placed.getWorld(), placed.getX(), placed.getY(), placed.getZ() - 1);
            case BOTTOM:
                return new Location(placed.getWorld(), placed.getX(), placed.getY() + 1, placed.getZ());
            case INVALID:
                return placed;
        }
        return null;
    }

    @Override
    public boolean pre() {
        return true;
    }

    @Override
    public void post() {
    }

    public enum PlaceType {
        PLACE_BLOCK, INTERACT_BLOCK
    }

    public enum BlockFace {
        NORTH(0, 0, -1, org.bukkit.block.BlockFace.NORTH),
        SOUTH(0, 0, 1, org.bukkit.block.BlockFace.SOUTH),
        EAST(1, 0, 0, org.bukkit.block.BlockFace.EAST),
        WEST(-1, 0, 0, org.bukkit.block.BlockFace.WEST),
        TOP(0, 1, 0, org.bukkit.block.BlockFace.UP),
        BOTTOM(0, -1, 0, org.bukkit.block.BlockFace.DOWN),
        INVALID(0, 0, 0, org.bukkit.block.BlockFace.SELF);

        @Getter
        private final int modX;
        @Getter
        private final int modY;
        @Getter
        private final int modZ;
        @Getter
        private final org.bukkit.block.BlockFace bukkitFace;

        BlockFace(final int modX, final int modY, final int modZ, final org.bukkit.block.BlockFace bukkitFace) {
            this.modX = modX;
            this.modY = modY;
            this.modZ = modZ;
            this.bukkitFace = bukkitFace;
        }

        public BlockFace getOppositeFace() {
            switch (this) {
                case NORTH:
                    return SOUTH;
                case SOUTH:
                    return NORTH;
                case EAST:
                    return WEST;
                case WEST:
                    return EAST;
                case TOP:
                    return BOTTOM;
                case BOTTOM:
                    return TOP;
                default:
                    return INVALID;
            }
        }
    }
}