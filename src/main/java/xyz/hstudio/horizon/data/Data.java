package xyz.hstudio.horizon.data;

import java.util.HashMap;
import java.util.Map;

public abstract class Data {

    // Type, VL, lastVL
    public Map<String, Float> violations = new HashMap<>();

    public long lastFailTick = 0;
}