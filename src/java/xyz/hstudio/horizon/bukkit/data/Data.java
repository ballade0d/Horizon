package xyz.hstudio.horizon.bukkit.data;

import xyz.hstudio.horizon.bukkit.util.Pair;

import java.util.HashMap;
import java.util.Map;

public abstract class Data {

    // Type, VL, lastVL
    public Map<String, Pair<Double, Double>> violationLevels = new HashMap<>();

    public long lastFailTick = 0;
}