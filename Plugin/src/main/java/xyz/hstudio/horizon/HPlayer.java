package xyz.hstudio.horizon;

import lombok.Getter;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.data.Physics;
import xyz.hstudio.horizon.module.check.CheckBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.ArrayList;
import java.util.List;

public class HPlayer {

    private static final Horizon inst = Horizon.getPlugin(Horizon.class);

    private final Player bPlayer;
    @Getter
    private final EntityBase base;
    @Getter
    private final List<CheckBase> checks;

    @Getter
    private final Physics physics;

    public HPlayer(Player bPlayer) {
        this.bPlayer = bPlayer;
        this.base = EntityBase.getEntity(bPlayer);
        this.checks = new ArrayList<>();

        this.physics = new Physics(this);

        inst.getPlayers().put(bPlayer.getUniqueId(), this);
    }

    public WorldBase getWorld() {
        return WorldBase.getWorld(bPlayer.getWorld());
    }
}