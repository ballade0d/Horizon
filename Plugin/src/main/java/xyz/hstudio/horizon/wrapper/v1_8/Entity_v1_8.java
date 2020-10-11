package xyz.hstudio.horizon.wrapper.v1_8;

import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

public class Entity_v1_8 extends EntityBase {

    protected final WorldBase world;
    protected final Entity entity;
    protected final org.bukkit.entity.Entity bukkitEntity;

    public Entity_v1_8(org.bukkit.entity.Entity entity) {
        this.world = WorldBase.getWorld(entity.getWorld());
        this.entity = ((CraftEntity) entity).getHandle();
        this.bukkitEntity = entity;
    }

    protected Entity_v1_8(Entity entity) {
        this.entity = entity;
        this.world = new World_v1_8(entity.world);
        this.bukkitEntity = entity.getBukkitEntity();
    }

    @Override
    public Location position() {
        return new Location(world, entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    @Override
    public AABB cube() {
        AxisAlignedBB nmsBox = entity.getBoundingBox();
        return new AABB(nmsBox.a, nmsBox.b, nmsBox.c, nmsBox.d, nmsBox.e, nmsBox.f);
    }

    @Override
    public float width() {
        return entity.width;
    }

    @Override
    public float length() {
        return entity.length;
    }

    @Override
    public org.bukkit.entity.Entity bukkit() {
        return bukkitEntity;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Entity_v1_8)) {
            return false;
        }
        return entity.getId() == ((Entity_v1_8) obj).entity.getId();
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}