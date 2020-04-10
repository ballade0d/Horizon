package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

public class InvalidMotionData extends Data {

    // Predict
    public float estimatedVelocity;
    public boolean prevGliding;
    // Magic
    public boolean magic;
    public long attemptGlideTick;
}