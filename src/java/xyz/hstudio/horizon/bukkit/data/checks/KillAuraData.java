package xyz.hstudio.horizon.bukkit.data.checks;

import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.data.Data;

import java.util.ArrayList;
import java.util.List;

public class KillAuraData extends Data {

    // TypeA
    public boolean lagging;
    public long lastMove;
    public int typeAFails;

    // TypeB
    public long startBlockTick;
    public long failTypeBTick;

    // TypeC
    public long startSprintTick;
    public long failTypeCTick;

    // TypeD
    public List<Vector> mouseMoves = new ArrayList<>();
    public List<Long> clickTicks = new ArrayList<>();

    // TypeE
    public float lastPitchChange;
}