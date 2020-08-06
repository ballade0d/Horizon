package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.outbound.AttributeEvent;

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
                player.moveModifiers.add(new AttributeEvent.AttributeModifier(AttributeEvent.SPRINT_UUID, 0.3, 2));
                break;
            case STOP_SPRINTING:
                player.isSprinting = false;
                break;
            default:
                break;
        }
    }

    public enum Action {
        START_SNEAKING, STOP_SNEAKING, START_SPRINTING, STOP_SPRINTING, START_GLIDING
    }
}