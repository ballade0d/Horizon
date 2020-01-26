package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;

public class ScaffoldData extends Data {

    // TypeC
    public boolean lagging;
    public long lastMove;
    public int typeCFails;

    // TypeD
    public long lastPlaceTick = -10000;
}