package xyz.hstudio.horizon.data.checks;

import xyz.hstudio.horizon.data.Data;

public class InventoryData extends Data {

    public boolean inventoryOpened;
    public boolean temporarilyBypass;
    public long inventoryOpenTick = -10000;
}