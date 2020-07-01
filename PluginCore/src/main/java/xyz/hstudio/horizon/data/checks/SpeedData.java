package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

public class SpeedData extends Data {

    // Predict
    public int noMoves;
    public double prevSpeed;
    public double discrepancies;
    public double negativeDiscrepancies;
    public double negativeDiscrepanciesCumulative;

    public long lastUseTick;
    public long lastToggleTick;
    public boolean attributeBypass;
    public boolean flagNextTick;
    // Sprint
    public long lastSprintTick;
    public boolean collisionHorizontal;
    public int sprintFails;
    // Strafe
    public int typeCFails;
    public int typeDFails;
    public long lastIdleTick;
}