package xyz.hstudio.horizon.bukkit.data.checks;

import xyz.hstudio.horizon.bukkit.data.Data;

public class AutoSwitchData extends Data {

    // TypeA
    public long lastPlaceTick = -10000;
    // TypeB
    public boolean usingItem;
    public long lastSwitchTick = -10000;
}