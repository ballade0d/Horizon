package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInChat;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;

public class ChatEvent extends Event<PacketPlayInChat> {

    public final String message;

    public ChatEvent(HPlayer p, String message) {
        super(p);
        this.message = message;
    }

    @Override
    public boolean pre() {
        return !message.startsWith("/") || !inst.getInGameCmd().onCommand(p, message);
    }
}