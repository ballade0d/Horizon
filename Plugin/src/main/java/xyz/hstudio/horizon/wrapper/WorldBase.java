package xyz.hstudio.horizon.wrapper;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import xyz.hstudio.horizon.util.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class WorldBase {

    protected final WorldServer worldServer;

    public WorldBase(org.bukkit.World bukkitWorld) {
        this.worldServer = ((CraftWorld) bukkitWorld).getHandle();
    }

    public WorldBase(net.minecraft.server.v1_8_R3.World world) {
        this.worldServer = world.getWorld().getHandle();
    }

    public org.bukkit.World bukkit() {
        return worldServer.getWorld();
    }

    public boolean isChunkLoaded(int x, int z) {
        return worldServer.chunkProviderServer.isChunkLoaded(x >> 4, z >> 4);
    }

    public boolean isChunkLoaded(Vector3D vec) {
        return worldServer.chunkProviderServer.isChunkLoaded(vec.getBlockX() >> 4, vec.getBlockZ() >> 4);
    }

    public BlockBase getBlock(int x, int y, int z) {
        Chunk chunk = worldServer.chunkProviderServer.getChunkIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }
        BlockPosition bPos = new BlockPosition(x, y, z);
        // return new BlockBase(this, chunk.getTypeAbs(x, y, z).getBlockData(), x, y, z); // This only get type
        return new BlockBase(this, chunk.getBlockData(bPos), bPos);
    }

    public BlockBase getBlock(Vector3D vec) {
        return getBlock(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }

    public List<EntityBase> getNearbyEntities(Vector3D vec, double x, double y, double z) {
        AxisAlignedBB bb = new AxisAlignedBB(vec.x - x, vec.y - y, vec.z - z, vec.x + x, vec.y + y, vec.z + z);
        List<Entity> entityList = worldServer.a((Entity) null, bb, null);
        List<EntityBase> entityBaseList = new ArrayList<>(entityList.size());
        for (Entity entity : entityList) {
            entityBaseList.add(new EntityBase(entity));
        }
        return entityBaseList;
    }

    public Entity getEntity(int id) {
        return worldServer.a(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WorldBase)) {
            return false;
        }
        return bukkit().getUID().equals(((WorldBase) obj).bukkit().getUID());
    }
}