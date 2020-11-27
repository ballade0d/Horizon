package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInCloseWindow;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;

public class WindowCloseEvent extends InEvent<PacketPlayInCloseWindow> {

    public final int id;

    public WindowCloseEvent(HPlayer p, int id) {
        super(p);
        this.id = id;
    }
}