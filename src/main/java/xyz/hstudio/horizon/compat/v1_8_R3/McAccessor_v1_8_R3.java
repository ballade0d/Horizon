package xyz.hstudio.horizon.compat.v1_8_R3;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelPipeline;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.IMcAccessor;
import xyz.hstudio.horizon.util.RandomUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class McAccessor_v1_8_R3 implements IMcAccessor {

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
        Entity nmsEntity = ((CraftWorld) world).getHandle().a(id);
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

        if (b instanceof BlockCarpet) {
            AABB[] aabbarr = new AABB[1];
            aabbarr[0] = new AABB(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY() + 0.0625, block.getZ() + 1);
            return aabbarr;
        }
        if (b instanceof BlockSnow && data.get(BlockSnow.LAYERS) == 1) {
            AABB[] aabbarr = new AABB[1];
            aabbarr[0] = new AABB(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY(), block.getZ() + 1);
            return aabbarr;
        }

        // Have to update shape
        b.updateShape(world, bPos);
        List<AxisAlignedBB> bbs = new ArrayList<>();
        AxisAlignedBB cube = new AxisAlignedBB(block.getX(), block.getY(), block.getZ(), block.getX() + 1, block.getY() + 1, block.getZ() + 1);
        b.a(world, bPos, data, cube, bbs, null);

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
        if (!b.getMaterial().isLiquid()) {
            return vec;
        }

        if (!world.areChunksLoaded(bPos, 1)) {
            return vec;
        }

        nmsVec = b.a(world, bPos, (Entity) null, nmsVec);
        vec.setX(nmsVec.a);
        vec.setY(nmsVec.b);
        vec.setZ(nmsVec.c);
        return vec;
    }

    @Override
    public void releaseItem(final Player player) {
        ((CraftPlayer) player).getHandle().bU();
    }

    @Override
    public boolean isSolid(final org.bukkit.block.Block block) {
        WorldServer world = ((CraftWorld) block.getWorld()).getHandle();
        Chunk chunk = world.getChunkIfLoaded(block.getX() >> 4, block.getZ() >> 4);
        if (chunk == null) {
            return block.getType().isSolid();
        }
        BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
        IBlockData data = chunk.getBlockData(bPos);
        return data.getBlock().getMaterial().isSolid();
    }

    @Override
    public boolean isCollidingEntities(final org.bukkit.World world, final Player player, final AABB aabb) {
        World w = ((CraftWorld) world).getHandle();
        return w.getEntities(((CraftPlayer) player).getHandle(), new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ))
                .size() > 0;
    }

    @Override
    public void setOnGround(final MoveEvent e, final boolean onGround) {
        try {
            int value = onGround ? 1 : 0;
            PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer(256));
            Packet<?> packet = (Packet<?>) e.rawPacket;
            packet.b(serializer);
            switch (e.moveType) {
                case FLYING:
                    serializer.setByte(0, value);
                    break;
                case POSITION:
                    serializer.setByte(24, value);
                    break;
                case LOOK:
                    serializer.setByte(8, value);
                    break;
                case POSITION_LOOK:
                    serializer.setByte(32, value);
                    break;
            }
            packet.a(serializer);
        } catch (Exception ignore) {
        }
    }
}