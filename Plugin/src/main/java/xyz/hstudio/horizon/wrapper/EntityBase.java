package xyz.hstudio.horizon.wrapper;

import org.bukkit.entity.Entity;
import xyz.hstudio.horizon.util.EnumVersion;
import xyz.hstudio.horizon.util.Location;

public abstract class EntityBase {

    public static EntityBase getEntity(Entity entity) {
        return EnumVersion.VERSION.getEntity(entity);
    }

    public abstract Location getPosition();
}