package xyz.hstudio.horizon.compat;

import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.events.Event;

public interface IPacketConverter {

    Event convertIn(HoriPlayer player, Object packet);

    Event convertOut(HoriPlayer player, Object packet);
}