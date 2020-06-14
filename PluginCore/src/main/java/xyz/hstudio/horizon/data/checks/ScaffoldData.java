package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

public class ScaffoldData extends Data {

    // TypeA
    public boolean lagging;
    public long lastMove;
    public int typeAFails;

    // TypeB
    public long lastStrafeTick;
    public int typeBFails;
}