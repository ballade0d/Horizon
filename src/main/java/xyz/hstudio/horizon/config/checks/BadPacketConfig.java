package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.config.CheckConfig;
import xyz.hstudio.horizon.config.annotation.Load;

public class BadPacketConfig extends CheckConfig<BadPacketConfig> {

    // TypeA
    @Load(file = "check.yml", path = "typeA.enabled")
    public boolean typeA_enabled = true;
    // TypeB
    @Load(file = "check.yml", path = "typeB.enabled")
    public boolean typeB_enabled = true;
    // TypeC
    @Load(file = "check.yml", path = "typeC.enabled")
    public boolean typeC_enabled = true;
    // TypeD
    @Load(file = "check.yml", path = "typeD.enabled")
    public boolean typeD_enabled = true;

    @Override
    public BadPacketConfig load() {
        return super.load(ModuleType.BadPacket.name().toLowerCase(), this);
    }
}