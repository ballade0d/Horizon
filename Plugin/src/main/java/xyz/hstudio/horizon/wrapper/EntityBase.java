package xyz.hstudio.horizon.wrapper;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;

public class EntityBase {

    protected final WorldBase world;
    protected final net.minecraft.server.v1_8_R3.Entity entity;
    protected final org.bukkit.entity.Entity bukkitEntity;

    public EntityBase(org.bukkit.entity.Entity entity) {
        this.world = new WorldBase(entity.getWorld());
        this.entity = ((CraftEntity) entity).getHandle();
        this.bukkitEntity = entity;
    }

    protected EntityBase(net.minecraft.server.v1_8_R3.Entity entity) {
        this.entity = entity;
        this.world = new WorldBase(entity.world);
        this.bukkitEntity = entity.getBukkitEntity();
    }

    public Location position() {
        return new Location(world, entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    public AABB cube() {
        AxisAlignedBB nmsBox = entity.getBoundingBox();
        return new AABB(nmsBox.a, nmsBox.b, nmsBox.c, nmsBox.d, nmsBox.e, nmsBox.f);
    }

    public AABB cube(Vector3D pos) {
        Vector3D move = position().subtract(pos);
        return cube().add(move);
    }

    public float width() {
        return entity.width;
    }

    public float length() {
        return entity.length;
    }

    public float borderSize() {
        return entity.ao();
    }

    public org.bukkit.entity.Entity bukkit() {
        return bukkitEntity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityBase)) {
            return false;
        }
        return entity.getId() == ((EntityBase) obj).entity.getId();
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}