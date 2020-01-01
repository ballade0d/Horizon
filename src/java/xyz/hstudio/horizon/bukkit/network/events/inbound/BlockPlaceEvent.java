package xyz.hstudio.horizon.bukkit.network.events.inbound;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.util.Location;

public class BlockPlaceEvent extends Event {

    public final Location placed;
    public final BlockFace face;
    public final Material material;
    public final Vector interaction;
    public final PlaceType placeType;

    public BlockPlaceEvent(final HoriPlayer player, final Location placed, final BlockFace face, final Material material, final Vector interaction, final PlaceType placeType, final WrappedPacket packet) {
        super(player, packet);
        this.placed = placed;
        this.face = face;
        this.material = material;
        this.interaction = interaction;
        this.placeType = placeType;
    }

    // placed.getFace(against)
    public Vector getPlaceBlockFace() {
        switch (face) {
            case TOP:
                return new Vector(0, -1, 0);
            case BOTTOM:
                return new Vector(0, 1, 0);
            case SOUTH:
                return new Vector(0, 0, -1);
            case NORTH:
                return new Vector(0, 0, 1);
            case WEST:
                return new Vector(1, 0, 0);
            case EAST:
                return new Vector(-1, 0, 0);
            case INVALID:
                return new Vector(0, 0, 0);
        }
        return null;
    }

    // against.getFace(placed)
    public Vector getTargetBlockFace() {
        switch (face) {
            case TOP:
                return new Vector(0, 1, 0);
            case BOTTOM:
                return new Vector(0, -1, 0);
            case SOUTH:
                return new Vector(0, 0, 1);
            case NORTH:
                return new Vector(0, 0, -1);
            case WEST:
                return new Vector(-1, 0, 0);
            case EAST:
                return new Vector(1, 0, 0);
            case INVALID:
                return new Vector(0, 0, 0);
        }
        return null;
    }

    public Location getTargetLocation() {
        switch (face) {
            case TOP:
                return new Location(placed.world, placed.x, placed.y - 1, placed.z);
            case EAST:
                return new Location(placed.world, placed.x - 1, placed.y, placed.z);
            case WEST:
                return new Location(placed.world, placed.x + 1, placed.y, placed.z);
            case NORTH:
                return new Location(placed.world, placed.x, placed.y, placed.z + 1);
            case SOUTH:
                return new Location(placed.world, placed.x, placed.y, placed.z - 1);
            case BOTTOM:
                return new Location(placed.world, placed.x, placed.y + 1, placed.z);
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