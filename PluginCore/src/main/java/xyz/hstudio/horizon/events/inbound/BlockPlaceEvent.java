package xyz.hstudio.horizon.events.inbound;

import org.bukkit.Material;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.util.enums.BlockFace;
import xyz.hstudio.horizon.util.wrap.ClientBlock;
import xyz.hstudio.horizon.util.wrap.Location;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class BlockPlaceEvent extends Event {

    public final Location placed;
    public final BlockFace face;
    public final Material material;
    public final Vector3D interaction;
    public final PlaceType placeType;

    public BlockPlaceEvent(final HoriPlayer player, final Location placed, final BlockFace face, final Material material, final Vector3D interaction, final PlaceType placeType) {
        super(player);
        this.placed = placed;
        this.face = face;
        this.material = material;
        this.interaction = interaction;
        this.placeType = placeType;
    }

    @Override
    public void post() {
        if (this.placeType == PlaceType.PLACE_BLOCK) {
            player.addClientBlock(placed, new ClientBlock(player.currentTick, material));
        }
    }

    // placed.getFace(against)
    public Vector3D getPlaceBlockFace() {
        switch (face) {
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

    // against.getFace(placed)
    public Vector3D getTargetBlockFace() {
        switch (face) {
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
            default:
                return placed;
        }
    }

    public enum PlaceType {
        PLACE_BLOCK, INTERACT_BLOCK
    }
}