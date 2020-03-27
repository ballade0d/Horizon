package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

public class SpeedData extends Data {

    // TypeA
    public int noMoves;
    public double prevSpeed;
    public double discrepancies;
    public double negativeDiscrepancies;
    public double negativeDiscrepanciesCumulative;
    // TypeB
    public long lastSprintTick;
    public boolean collisionHorizontal;
    // TypeC
    public int typeCFails;
}