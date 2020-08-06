package xyz.hstudio.horizon.event.inbound;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.event.Event;

public class ClientCommandEvent extends Event {

    public final ClientCommand command;

    public ClientCommandEvent(final HoriPlayer player, final ClientCommand command) {
        super(player);
        this.command = command;
    }

    public enum ClientCommand {
        PERFORM_RESPAWN, REQUEST_STATS, OPEN_INVENTORY_ACHIEVEMENT
    }
}