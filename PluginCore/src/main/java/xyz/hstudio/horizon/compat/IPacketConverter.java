package xyz.hstudio.horizon.compat;

import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

public interface IPacketConverter {

    Event convertIn(HoriPlayer player, Object packet);

    Event convertOut(HoriPlayer player, Object packet);
}