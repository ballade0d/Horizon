package xyz.hstudio.horizon.bukkit.network.events.inbound;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class ActionEvent extends Event {

    public Action action;

    public ActionEvent(final HoriPlayer player, final Action action, final WrappedPacket packet) {
        super(player, packet);
        this.action = action;
    }

    @Override
    public boolean pre() {
        return true;
    }

    @Override
    public void post() {
        switch (this.action) {
            case START_SNEAKING:
                player.isSneaking = true;
                break;
            case STOP_SNEAKING:
                player.isSneaking = false;
                break;
            case START_SPRINTING:
                player.isSprinting = true;
                break;
            case STOP_SPRINTING:
                player.isSprinting = false;
                break;
            case START_GLIDING:
                player.isGliding = true;
                break;
        }
    }

    public enum Action {
        START_SNEAKING, STOP_SNEAKING, START_SPRINTING, STOP_SPRINTING, START_GLIDING
    }
}