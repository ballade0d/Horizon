package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;
import xyz.hstudio.horizon.bukkit.util.Vector2D;

import java.util.ArrayList;
import java.util.List;

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
    // TypeF
    public final List<Vector2D> vector2DList = new ArrayList<>(16);
}