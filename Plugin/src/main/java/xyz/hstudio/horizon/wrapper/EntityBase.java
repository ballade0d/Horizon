package xyz.hstudio.horizon.wrapper;

import org.bukkit.entity.Entity;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.enums.Version;

public abstract class EntityBase {

    public static EntityBase getEntity(Entity entity) {
        return Version.VERSION.getEntity(entity);
    }

    public abstract Location getPosition();
}