package xyz.hstudio.horizon.util.enums;

import lombok.Getter;

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