package xyz.hstudio.horizon.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.hstudio.horizon.api.enums.Detection;

public class PlayerViolationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player player;
    @Getter
    private final Detection detection;
    @Getter
    private final String type;
    @Getter
    private final int violation;
    @Getter
    private final String info;
    @Getter
    @Setter
    private boolean cancelled;

    public PlayerViolationEvent(Player player, Detection detection, String type, int violation, String info) {
        super(true);
        this.player = player;
        this.detection = detection;
        this.type = type;
        this.violation = violation;
        this.info = info;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}