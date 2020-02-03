package xyz.hstudio.horizon.compat.v1_12_R1;

import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.compat.IMcAccessor;
import xyz.hstudio.horizon.util.RandomUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class McAccessor_v1_12_R1 implements IMcAccessor {

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
        BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        return chunk.getBlockData(bPos).getBlock().frictionFactor;
    }

    @Override
    public AABB getCube(final org.bukkit.entity.Entity entity) {
        AxisAlignedBB cube = ((CraftEntity) entity).getHandle().getBoundingBox();
        return new AABB(cube.a, cube.b, cube.c, cube.d, cube.e, cube.f);
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

    @Override
    public AABB[] getBoxes(final org.bukkit.block.Block block) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        Chunk chunk = world.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        if (chunk == null) {
            return new AABB[0];
        }
        BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData data = chunk.getBlockData(bPos);
        Block b = data.getBlock();

        if (b instanceof BlockSnow && data.get(BlockSnow.LAYERS) == 1) {
            AABB[] aabbarr = new AABB[1];
            aabbarr[0] = new AABB(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY(), block.getZ() + 1);
            return aabbarr;
        }

        List<AxisAlignedBB> bbs = new ArrayList<>();
        AxisAlignedBB cube = new AxisAlignedBB(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY() + 1, block.getZ() + 1);
        b.a(data, world, bPos, cube, bbs, null, false);

        AxisAlignedBB[] raw = bbs.toArray(new AxisAlignedBB[0]);
        AABB[] boxes = new AABB[bbs.size()];

        for (int i = 0; i < bbs.size(); i++) {
            boxes[i] = new AABB(raw[i].a, raw[i].b, raw[i].c, raw[i].d, raw[i].e, raw[i].f);
        }

        return boxes;
    }

    @Override
    public double getMoveFactor(final Player player) {
        return ((CraftPlayer) player).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
    }

    @Override
    public Object newTransactionPacket() {
        return new PacketPlayOutTransaction(0, RandomUtils.nextShort(), false);
    }

    @Override
    public Vector3D getFlowDirection(final org.bukkit.block.Block block) {
        Vector3D vec = new Vector3D();

        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        Chunk chunk = world.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        if (chunk == null) {
            return vec;
        }
        BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData data = chunk.getBlockData(bPos);
        Block b = data.getBlock();

        Vec3D nmsVec = new Vec3D(0, 0, 0);
        if (!b.q(data).isLiquid()) {
            return vec;
        }

        if (!world.areChunksLoaded(bPos, 1)) {
            return vec;
        }

        nmsVec = b.a(world, bPos, null, nmsVec);
        vec.setX(nmsVec.x);
        vec.setY(nmsVec.y);
        vec.setZ(nmsVec.z);
        return vec;
    }
}