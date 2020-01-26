package xyz.hstudio.horizon.bukkit.compat;

import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;

public interface IPacketConverter {

    Event convertIn(final HoriPlayer player, final Object packet);

    Event convertOut(final HoriPlayer player, final Object packet);
}