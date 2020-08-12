package xyz.hstudio.horizon.wrapper;

import org.bukkit.World;
import xyz.hstudio.horizon.util.EnumVersion;
import xyz.hstudio.horizon.util.Vec3D;

import java.util.List;

public abstract class WorldBase {

    public static WorldBase getWorld(World world) {
        return EnumVersion.VERSION.getWorld(world);
    }

    public abstract boolean isChunkLoaded(int x, int z);

    public abstract boolean isChunkLoaded(Vec3D vec);

    public abstract BlockBase getBlock(Vec3D vec);

    public abstract List<EntityBase> getNearbyEntities(Vec3D vec, double x, double y, double z);
}