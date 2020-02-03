package xyz.hstudio.horizon.config.checks;

import xyz.hstudio.horizon.config.Config;
import xyz.hstudio.horizon.config.annotation.Load;

public class KillAuraConfig extends Config {

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
    // TypeE
    @Load(file = "check.yml", path = "typeE.enabled")
    public boolean typeE_enabled = true;
    // TypeF
    @Load(file = "check.yml", path = "typeF.enabled")
    public boolean typeF_enabled = true;
}