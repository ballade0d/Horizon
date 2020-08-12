package xyz.hstudio.horizon;

import lombok.Getter;
import org.bukkit.entity.Player;
import xyz.hstudio.horizon.api.EnumCheck;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;

import java.util.EnumMap;
import java.util.Map;

public class HPlayer {

    private final Player bPlayer;
    @Getter
    private final EntityBase base;
    @Getter
    private final Map<EnumCheck, Integer> violations;
    @Getter
    private final Map<EnumCheck, Long> failTicks;
    public Location position;

    public HPlayer(Player bPlayer) {
        this.bPlayer = bPlayer;
        this.base = EntityBase.getEntity(bPlayer);
        this.violations = new EnumMap<>(EnumCheck.class);
        this.failTicks = new EnumMap<>(EnumCheck.class);
        this.position = base.getPosition();
    }

    public WorldBase getWorld() {
        return WorldBase.getWorld(bPlayer.getWorld());
    }
}