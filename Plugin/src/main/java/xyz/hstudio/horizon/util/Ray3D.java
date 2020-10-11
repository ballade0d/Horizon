package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import xyz.hstudio.horizon.Horizon;

import java.util.Objects;

@AllArgsConstructor
public class Ray3D {

    @Getter
    protected final Vector3D origin, direction;

    public Vector3D getPointAtDistance(double distance) {
        return new Vector3D(
                origin.x + direction.x * distance,
                origin.y + direction.y * distance,
                origin.z + direction.z * distance
        );
    }

    public void highlight(World world, double blocksAway, double accuracy) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Horizon.getPlugin(Horizon.class), () -> {
            for (double x = 0; x < blocksAway; x += accuracy) {
                Vector3D vec = getPointAtDistance(x);
                Location loc = new org.bukkit.Location(world, vec.x, vec.y, vec.z);
                world.playEffect(loc, Effect.COLOURED_DUST, 1);
            }
        }, 0L);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Ray3D)) {
            return false;
        }
        Ray3D other = (Ray3D) obj;
        return Objects.equals(origin, other.origin) && Objects.equals(direction, other.direction);
    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }
}