package xyz.hstudio.horizon.data;

import xyz.hstudio.horizon.util.collect.Pair;

import java.util.HashMap;
import java.util.Map;

public abstract class Data {

    // Type, VL, lastVL
    public Map<String, Pair<Double, Double>> violationLevels = new HashMap<>();

    public long lastFailTick = 0;
}