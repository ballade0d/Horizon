package xyz.hstudio.horizon.wrapper.v1_8;

import lombok.val;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.BlockBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.ArrayList;
import java.util.List;

public class World_v1_8 extends WorldBase {

    protected final WorldServer worldServer;

    public World_v1_8(org.bukkit.World bukkitWorld) {
        this.worldServer = ((CraftWorld) bukkitWorld).getHandle();
    }

    public World_v1_8(World world) {
        this.worldServer = world.getWorld().getHandle();
    }

    @Override
    public org.bukkit.World bukkit() {
        return worldServer.getWorld();
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        return worldServer.chunkProviderServer.isChunkLoaded(x >> 4, z >> 4);
    }

    @Override
    public boolean isChunkLoaded(Vector3D vec) {
        return worldServer.chunkProviderServer.isChunkLoaded(vec.getBlockX() >> 4, vec.getBlockZ() >> 4);
    }

    @Override
    public BlockBase getBlock(Vector3D vec) {
        val x = vec.getBlockX();
        val y = vec.getBlockY();
        val z = vec.getBlockZ();
        Chunk chunk = worldServer.chunkProviderServer.getChunkIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }
        return new Block_v1_8(this, chunk.getTypeAbs(x, y, z).getBlockData(), x, y, z);
    }

    @Override
    public List<EntityBase> getNearbyEntities(Vector3D vec, double x, double y, double z) {
        AxisAlignedBB bb = new AxisAlignedBB(vec.x - x, vec.y - y, vec.z - z, vec.x + x, vec.y + y, vec.z + z);
        List<Entity> entityList = worldServer.a((Entity) null, bb, null);
        List<EntityBase> entityBaseList = new ArrayList<>(entityList.size());
        for (Entity entity : entityList) {
            entityBaseList.add(new Entity_v1_8(entity));
        }
        return entityBaseList;
    }
}