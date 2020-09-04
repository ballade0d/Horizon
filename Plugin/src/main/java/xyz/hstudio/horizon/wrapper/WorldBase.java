package xyz.hstudio.horizon.wrapper;

import org.bukkit.World;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Version;

import java.util.List;

public abstract class WorldBase {

    public static WorldBase getWorld(World world) {
        return Version.VERSION.getWorld(world);
    }

    public abstract boolean isChunkLoaded(int x, int z);

    public abstract boolean isChunkLoaded(Vector3D vec);

    public abstract BlockBase getBlock(Vector3D vec);

    public abstract List<EntityBase> getNearbyEntities(Vector3D vec, double x, double y, double z);
}