package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

public class InteractData extends Data {

    // TypeC
    public boolean lagging;
    public long lastMove;
    public int typeCFails;

    // TypeD
    public long lastPlaceTick = -10000;
    public int typeDFails;
}