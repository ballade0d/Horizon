package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.event.InEvent;

@RequiredArgsConstructor
@Getter
public class AbilitiesEvent extends InEvent {

    private final boolean invulnerable;
    private final boolean flying;
    private final boolean flyable;
    private final boolean inCreative;
    private final float flyingSpeed;
    private final float walkingSpeed;
}