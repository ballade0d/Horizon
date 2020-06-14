package xyz.hstudio.horizon.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.hstudio.horizon.module.Module;

import java.util.Arrays;

public class PlayerViolateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Getter
    private final Player player;
    @Getter
    private final ModuleType type;
    @Getter
    private final String module;
    @Getter
    private final String debug;
    @Getter
    private final float nowViolation;
    @Getter
    private final float oldViolation;
    @Getter
    @Setter
    private boolean cancelled;

    public PlayerViolateEvent(final Player player, final ModuleType type, final String module, final String[] debug, final float nowViolation, final float oldViolation) {
        super(!Bukkit.isPrimaryThread());
        if (Arrays.stream(Thread.currentThread().getStackTrace())
                .noneMatch(element -> element.getClassName().equals(Module.class.getName()))) {
            throw new IllegalStateException("This event should not be instantiated here.");
        }
        this.player = player;
        this.type = type;
        this.module = module;
        this.debug = debug.length == 0 ? "" : Arrays.toString(debug);
        this.nowViolation = nowViolation;
        this.oldViolation = oldViolation;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}