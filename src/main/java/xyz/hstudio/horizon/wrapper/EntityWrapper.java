package xyz.hstudio.horizon.wrapper;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Vector3D;

public class EntityWrapper {

    public final WorldWrapper world;
    protected final net.minecraft.server.v1_8_R3.Entity entity;

    public EntityWrapper(org.bukkit.entity.Entity entity) {
        this.world = new WorldWrapper(entity.getWorld());
        this.entity = ((CraftEntity) entity).getHandle();
    }

    public EntityWrapper(net.minecraft.server.v1_8_R3.Entity entity) {
        this.entity = entity;
        this.world = new WorldWrapper(entity.world);
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
        if (!(obj instanceof EntityWrapper)) {
            return false;
        }
        return entity.getId() == ((EntityWrapper) obj).entity.getId();
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}