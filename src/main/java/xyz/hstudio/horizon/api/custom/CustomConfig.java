package xyz.hstudio.horizon.api.custom;

import xyz.hstudio.horizon.file.Load;

public abstract class CustomConfig {

    @Load(path = "enabled")
    public boolean enabled = true;
}