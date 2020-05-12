package xyz.hstudio.horizon.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.hstudio.horizon.module.Module;

import java.util.Arrays;

public class PlayerViolateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Getter
    private final Player player;
    @Getter
    private final ModuleType type;
    @Getter
    private final float nowViolation;
    @Getter
    private final float oldViolation;
    @Getter
    @Setter
    private boolean cancelled;

    public PlayerViolateEvent(final Player player, final ModuleType type, final float nowViolation, final float oldViolation) {
        super(true);
        if (Arrays.stream(Thread.currentThread().getStackTrace())
                .noneMatch(element -> element.getClassName().equals(Module.class.getName()))) {
            throw new IllegalStateException("This event should not be instantiated here.");
        }
        this.player = player;
        this.type = type;
        this.nowViolation = nowViolation;
        this.oldViolation = oldViolation;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}