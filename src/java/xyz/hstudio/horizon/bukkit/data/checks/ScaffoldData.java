package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;

public class ScaffoldData extends Data {

    //TypeD
    public boolean lagging;
    public long lastMove;
    public int typeDFails;
    // TypeE
    public long lastPlaceTick = Long.MIN_VALUE;
}