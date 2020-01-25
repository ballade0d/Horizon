package xyz.hstudio.horizon.bukkit.network.events.inbound;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;

public class ClientCommandEvent extends Event {

    public final ClientCommand command;

    public ClientCommandEvent(final HoriPlayer player, final ClientCommand command, final WrappedPacket packet) {
        super(player, packet);
        this.command = command;
    }

    public enum ClientCommand {
        PERFORM_RESPAWN, REQUEST_STATS, OPEN_INVENTORY_ACHIEVEMENT
    }
}