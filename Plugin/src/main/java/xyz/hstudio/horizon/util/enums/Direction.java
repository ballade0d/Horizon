package xyz.hstudio.horizon.util.enums;

public enum Direction {
    UP(false),
    DOWN(false),
    NORTH(true),
    SOUTH(true),
    WEST(true),
    EAST(true);

    private final boolean horizontal;

    Direction(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public boolean horizontal() {
        return horizontal;
    }
}