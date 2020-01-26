package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;

import java.util.ArrayList;
import java.util.List;

public class KillAuraData extends Data {

    // TypeA
    public boolean lagging;
    public long lastMove;
    public int typeAFails;

    // TypeB
    public long startBlockTime;
    public long failTypeBTime;

    // TypeC
    public long startSprintTick;
    public long failTypeCTick;

    // TypeD
    public float lastPitchChange;
    public int gcdFails;

    // TypeE
    public List<Integer> moveInterval = new ArrayList<>();
    public int moves;
    public boolean swung;

    // TypeF, TypeG
    public long lastHitTick = -10000;
}