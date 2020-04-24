package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.util.wrap.Location;

public class InvalidMotionData extends Data {

    // Predict
    public float estimatedVelocity;
    public boolean prevGliding;
    // Magic
    public boolean magic;
    public long attemptGlideTick;
    // Step
    public Location safeLoc;
}