package xyz.hstudio.horizon.wrapper.v1_8;

import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

public class Entity_v1_8 extends EntityBase {

    private final WorldBase world;
    private final Entity entity;
    private final org.bukkit.entity.Entity bukkitEntity;

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
    public Location getPosition() {
        return new Location(world, entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntity() {
        return bukkitEntity;
    }
}