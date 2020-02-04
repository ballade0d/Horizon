package xyz.hstudio.horizon.api.events.inbound;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

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