package xyz.hstudio.horizon.event;

import lombok.Getter;
import lombok.Setter;

public abstract class OutEvent {

    @Getter
    @Setter
    private boolean cancelled = false;
}