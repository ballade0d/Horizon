package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

import java.util.Deque;
import java.util.LinkedList;

public class AutoClickerData extends Data {

    // TypeA
    public long lastHitTick;
    public final Deque<Long> samplesA = new LinkedList<>();
}