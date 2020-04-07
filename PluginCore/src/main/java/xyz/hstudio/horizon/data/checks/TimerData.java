package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

public class TimerData extends Data {

    public long prevMoveTime = System.nanoTime();
    public long drift = 0;
    public int fails;
}