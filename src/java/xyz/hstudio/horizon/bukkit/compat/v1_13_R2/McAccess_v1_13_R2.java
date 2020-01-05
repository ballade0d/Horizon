package xyz.hstudio.horizon.bukkit.compat.v1_13_R2;

import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.Chunk;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.lib.com.esotericsoftware.reflectasm.FieldAccess;

public class McAccess_v1_13_R2 extends McAccess {

    private final FieldAccess AABB = FieldAccess.get(AxisAlignedBB.class);

    @Override
    public ChannelPipeline getPipeline(final Player player) {
        return ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
    }

    @Override
    public float sin(final float v) {
        return MathHelper.sin(v);
    }

    @Override
    public float cos(final float v) {
        return MathHelper.cos(v);
    }

    @Override
    public boolean isAccumulated(final Player player) {
        return true;
    }

    @Override
    public float getFriction(final org.bukkit.block.Block block) {
        Chunk chunk = ((CraftWorld) block.getWorld()).getHandle().getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        if (chunk == null) {
            return 1.0F;
        }
        return chunk.getBlockData(block.getX(), block.getY(), block.getZ()).getBlock().n();
    }

    @Override
    public xyz.hstudio.horizon.bukkit.util.AxisAlignedBB getCube(final Player player) {
        AxisAlignedBB cube = ((CraftPlayer) player).getHandle().getBoundingBox();
        // It has different field name between Spigot/Paper. I have to use reflection.
        double minX = AABB.getDouble(cube, 0);
        double minY = AABB.getDouble(cube, 1);
        double minZ = AABB.getDouble(cube, 2);
        double maxX = AABB.getDouble(cube, 3);
        double maxY = AABB.getDouble(cube, 4);
        double maxZ = AABB.getDouble(cube, 5);
        return new xyz.hstudio.horizon.bukkit.util.AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void ensureMainThread(final Runnable task) {
        ((CraftServer) Bukkit.getServer()).getServer().processQueue.add(() -> {
            try {
                task.run();
            } catch (Throwable throwable) {
                // Have to cache any throwable or the server will crash if an error occurs.
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public org.bukkit.entity.Entity getEntity(org.bukkit.World world, int id) {
        Entity nmsEntity = ((CraftWorld) world).getHandle().getEntity(id);
        return nmsEntity == null ? null : nmsEntity.getBukkitEntity();
    }
}