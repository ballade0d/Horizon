package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class KillAuraData extends Data {

    // TypeA
    public boolean lagging;
    public long lastMove;
    public int typeAFails;
    // TypeB
    public long startSprintTick;
    public long failTypeBTick;
    // TypeC
    public float lastPitchChange;
    public int gcdFails;
    // TypeD
    public long lastHitTick = -10000;
    // TypeE
    public Vector3D intersection;
}