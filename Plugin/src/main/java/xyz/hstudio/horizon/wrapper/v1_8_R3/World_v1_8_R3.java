package xyz.hstudio.horizon.wrapper.v1_8_R3;

import lombok.val;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import xyz.hstudio.horizon.util.Vec3D;
import xyz.hstudio.horizon.wrapper.BlockBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.ArrayList;
import java.util.List;

public class World_v1_8_R3 extends WorldBase {

    protected final WorldServer worldServer;

    public World_v1_8_R3(org.bukkit.World bukkitWorld) {
        this.worldServer = ((CraftWorld) bukkitWorld).getHandle();
    }

    public World_v1_8_R3(World world) {
        this.worldServer = world.getWorld().getHandle();
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        return worldServer.chunkProviderServer.isChunkLoaded(x >> 4, z >> 4);
    }

    @Override
    public boolean isChunkLoaded(Vec3D vec) {
        return worldServer.chunkProviderServer.isChunkLoaded(vec.getBlockX() >> 4, vec.getBlockZ() >> 4);
    }

    @Override
    public BlockBase getBlock(Vec3D vec) {
        val x = vec.getBlockX();
        val y = vec.getBlockY();
        val z = vec.getBlockZ();
        Chunk chunk = worldServer.chunkProviderServer.getChunkIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }
        return new Block_v1_8_R3(this, chunk.getTypeAbs(x, y, z).getBlockData());
    }

    @Override
    public List<EntityBase> getNearbyEntities(Vec3D vec, double x, double y, double z) {
        AxisAlignedBB bb = new AxisAlignedBB(vec.getX() - x, vec.getY() - y, vec.getZ() - z, vec.getX() + x, vec.getY() + y, vec.getZ() + z);
        List<Entity> entityList = worldServer.a((Entity) null, bb, null);
        List<EntityBase> entityBaseList = new ArrayList<>(entityList.size());
        for (Entity entity : entityList) {
            entityBaseList.add(new Entity_v1_8_R3(entity));
        }
        return entityBaseList;
    }
}