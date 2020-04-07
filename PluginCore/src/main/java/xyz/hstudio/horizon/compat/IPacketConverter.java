package xyz.hstudio.horizon.compat;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public interface IPacketConverter {

    Event convertIn(final HoriPlayer player, final Object packet);

    Event convertOut(final HoriPlayer player, final Object packet);
}