package xyz.hstudio.horizon.wrapper.v1_12;

import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.BlockBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.ArrayList;
import java.util.List;

public class World_v1_12 extends WorldBase {

    protected final WorldServer worldServer;

    public World_v1_12(org.bukkit.World bukkitWorld) {
        this.worldServer = ((CraftWorld) bukkitWorld).getHandle();
    }

    public World_v1_12(World world) {
        this.worldServer = world.getWorld().getHandle();
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        return worldServer.getChunkProviderServer().isLoaded(x >> 4, z >> 4);
    }

    @Override
    public boolean isChunkLoaded(Vector3D vec) {
        return worldServer.getChunkProviderServer().isLoaded(vec.getBlockX() >> 4, vec.getBlockZ() >> 4);
    }

    @Override
    public BlockBase getBlock(Vector3D vec) {
        val x = vec.getBlockX();
        val y = vec.getBlockY();
        val z = vec.getBlockZ();
        Chunk chunk = worldServer.getChunkProviderServer().getChunkIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }
        return new Block_v1_12(this, chunk.a(x, y, z), x, y, z);
    }

    @Override
    public List<EntityBase> getNearbyEntities(Vector3D vec, double x, double y, double z) {
        AxisAlignedBB bb = new AxisAlignedBB(vec.getX() - x, vec.getY() - y, vec.getZ() - z, vec.getX() + x, vec.getY() + y, vec.getZ() + z);
        List<Entity> entityList = worldServer.getEntities(null, bb, null);
        List<EntityBase> entityBaseList = new ArrayList<>(entityList.size());
        for (Entity entity : entityList) {
            entityBaseList.add(new Entity_v1_12(entity));
        }
        return entityBaseList;
    }
}