package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

import java.util.ArrayList;
import java.util.List;

public class AutoClickerData extends Data {

    // TypeA
    public final List<Long> deltaTicks = new ArrayList<>();
    public final List<Double> hitSamples = new ArrayList<>();
    public long prevHitTick;
}