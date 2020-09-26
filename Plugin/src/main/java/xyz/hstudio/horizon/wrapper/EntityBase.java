package xyz.hstudio.horizon.wrapper;

import org.bukkit.entity.Entity;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.enums.Version;

public abstract class EntityBase {

    public static EntityBase getEntity(Entity entity) {
        return Version.getInst().getEntity(entity);
    }

    public abstract Location position();

    public abstract AABB cube();

    public abstract float width();

    public abstract float length();

    public abstract Entity bukkit();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}