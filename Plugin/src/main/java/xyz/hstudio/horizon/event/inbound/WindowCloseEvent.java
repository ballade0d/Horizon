package xyz.hstudio.horizon.event.inbound;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.event.InEvent;

@RequiredArgsConstructor
@Getter
public class WindowCloseEvent extends InEvent {

    private final int id;
}