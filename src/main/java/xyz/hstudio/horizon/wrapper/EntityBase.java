package xyz.hstudio.horizon.wrapper;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;

public class EntityBase {

    protected final WorldBase world;
    protected final net.minecraft.server.v1_8_R3.Entity entity;

    public EntityBase(org.bukkit.entity.Entity entity) {
        this.world = new WorldBase(entity.getWorld());
        this.entity = ((CraftEntity) entity).getHandle();
    }

    public EntityBase(net.minecraft.server.v1_8_R3.Entity entity) {
        this.entity = entity;
        this.world = new WorldBase(entity.world);
    }

    public Location position() {
        return new Location(world, entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    public AABB cube(Vector3D pos) {
        Vector3D move = position().subtract(pos);
        return move.toAABB();
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