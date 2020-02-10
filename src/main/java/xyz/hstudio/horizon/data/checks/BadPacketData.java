package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;
import xyz.hstudio.horizon.util.wrap.Location;

public class BadPacketData extends Data {

    // TypeA
    public long lastTick;
    // TypeB
    public int flyingCount;
    // TypeC
    public long lastPayloadTime;
    // TypeD
    public Location legitLocation;
}