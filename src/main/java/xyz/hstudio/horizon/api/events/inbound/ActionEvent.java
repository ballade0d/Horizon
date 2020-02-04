package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public class ActionEvent extends Event {

    public Action action;

    public ActionEvent(final HoriPlayer player, final Action action) {
        super(player);
        this.action = action;
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
        }
    }

    public enum Action {
        START_SNEAKING, STOP_SNEAKING, START_SPRINTING, STOP_SPRINTING, START_GLIDING
    }
}