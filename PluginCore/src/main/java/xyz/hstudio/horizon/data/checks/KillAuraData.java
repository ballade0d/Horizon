package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.UUID;

public class KillAuraData extends Data {

    public long lastHitTick = -10000;
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
    public long lastStrafeTick;
    public int directionFails;
    // InteractAutoBlock
    public Vector3D intersection;
    // NormalAutoBlock
    public boolean interactEntity;
    public long lastIntersectTick = -10000;
    // Multi
    public UUID prevHit;
    // KeepSprint
    public long failKeepSprintTick;
    //
    public long lastHitTime;
    public int lastDelay;
}