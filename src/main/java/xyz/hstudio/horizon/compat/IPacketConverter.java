package xyz.hstudio.horizon.compat;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.network.events.Event;

public interface IPacketConverter {

    Event convertIn(final HoriPlayer player, final Object packet);

    Event convertOut(final HoriPlayer player, final Object packet);
}