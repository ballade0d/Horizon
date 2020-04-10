package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class KillAuraData extends Data {

    // Order
    public boolean lagging;
    public long lastMove;
    public int typeAFails;
    // SuperKb
    public long startSprintTick;
    public long failTypeBTick;
    // GCD
    public float lastPitchChange;
    public int gcdFails;
    // Direction
    public long lastHitTickD = -10000;
    public int typeDFails;
    // InteractAutoBlock
    public Vector3D intersection;
    // NormalAutoBlock
    public boolean interactEntity;
    public long lastHitTickF = -10000;
}