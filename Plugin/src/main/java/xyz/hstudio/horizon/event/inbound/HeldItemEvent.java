package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.event.InEvent;

@RequiredArgsConstructor
@Getter
public class HeldItemEvent extends InEvent {

    private final int heldItemSlot;
}