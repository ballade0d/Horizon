package xyz.hstudio.horizon.wrapper;

import org.bukkit.Material;

public abstract class BlockBase {

    public abstract Material type();

    public abstract boolean isSolid();

    public abstract float hardness();

    public abstract float friction();
}