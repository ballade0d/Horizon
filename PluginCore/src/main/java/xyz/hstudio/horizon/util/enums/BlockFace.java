package xyz.hstudio.horizon.util.enums;

import lombok.Getter;

public enum BlockFace {
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    EAST(1, 0, 0),
    WEST(-1, 0, 0),
    TOP(0, 1, 0),
    BOTTOM(0, -1, 0),
    INVALID(0, 0, 0);

    @Getter
    private final int modX;
    @Getter
    private final int modY;
    @Getter
    private final int modZ;

    BlockFace(final int modX, final int modY, final int modZ) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
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