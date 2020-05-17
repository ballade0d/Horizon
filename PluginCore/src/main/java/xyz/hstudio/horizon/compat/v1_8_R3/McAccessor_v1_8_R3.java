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
import xyz.hstudio.horizon.compat.IMcAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.inbound.MoveEvent;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Location;

import java.util.ArrayList;

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
    public void releaseItem(final Player player) {
        ((CraftPlayer) player).getHandle().bU();
    }

    @Override
    public boolean isCollidingEntities(final org.bukkit.World world, final Player player, final AABB aabb) {
        World w = ((CraftWorld) world).getHandle();
        return w.getEntities(((CraftPlayer) player).getHandle(), new AxisAlignedBB(aabb.minX + 0.1, aabb.minY + 0.1, aabb.minZ + 0.1, aabb.maxX + 0.1, aabb.maxY + 0.1, aabb.maxZ + 0.1))
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
                default:
                    return;
            }
            packet.a(serializer);
        } catch (Exception ignore) {
        }
    }

    @Override
    public Object createExplosionPacket(final double x, final double y, final double z) {
        return new PacketPlayOutExplosion(x, y, z, 0, new ArrayList<>(), new Vec3D(0, 0, 0));
    }

    @Override
    public void updateBlock(final HoriPlayer player, final Location loc) {
        World world = ((CraftWorld) loc.world).getHandle();
        BlockPosition bPos = new BlockPosition(loc.x, loc.y, loc.z);
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(world, bPos);
        player.sendPacket(packet);
    }

    @Override
    public int getPing(final Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public void showPlayer(final Player player, final Player show) {
        EntityPlayer observer = ((CraftPlayer) player).getHandle();
        EntityTracker tracker = ((WorldServer) observer.world).tracker;
        EntityPlayer other = ((CraftPlayer) show).getHandle();
        EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
        if (entry != null && !entry.trackedPlayers.contains(observer)) {
            entry.updatePlayer(observer);
        }
    }

    @Override
    public void hidePlayer(final Player player, final Player hide) {
        EntityPlayer observer = ((CraftPlayer) player).getHandle();
        EntityTracker tracker = ((WorldServer) observer.world).tracker;
        EntityPlayer other = ((CraftPlayer) hide).getHandle();
        EntityTrackerEntry entry = tracker.trackedEntities.get(other.getId());
        if (entry != null) {
            entry.clear(observer);
        }
    }
}