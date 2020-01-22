package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;

public class TimerData extends Data {

    public long prevMoveTime = System.nanoTime();
    public long drift = 0;
}