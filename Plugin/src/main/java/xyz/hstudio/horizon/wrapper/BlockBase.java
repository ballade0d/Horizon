package xyz.hstudio.horizon.wrapper;

import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.util.AABB;

public abstract class BlockBase {

    public abstract Material type();

    public abstract boolean isSolid();

    public abstract boolean isLiquid();

    public abstract float hardness();

    public abstract float friction();

    public abstract AABB[] boxes(HPlayer p);
}