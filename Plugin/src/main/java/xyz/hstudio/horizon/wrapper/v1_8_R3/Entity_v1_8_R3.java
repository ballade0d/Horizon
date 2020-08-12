package xyz.hstudio.horizon.wrapper.v1_8_R3;

import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

public class Entity_v1_8_R3 extends EntityBase {

    private final WorldBase world;
    private final Entity entity;

    public Entity_v1_8_R3(WorldBase world, org.bukkit.entity.Entity entity) {
        this.world = world;
        this.entity = ((CraftEntity) entity).getHandle();
    }

    public Entity_v1_8_R3(org.bukkit.entity.Entity entity) {
        this.world = WorldBase.getWorld(entity.getWorld());
        this.entity = ((CraftEntity) entity).getHandle();
    }

    protected Entity_v1_8_R3(Entity entity) {
        this.entity = entity;
        this.world = new World_v1_8_R3(entity.world);
    }

    @Override
    public Location getPosition() {
        return new Location(world, entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }
}