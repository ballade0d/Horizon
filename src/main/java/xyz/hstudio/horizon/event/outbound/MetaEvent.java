package xyz.hstudio.horizon.event.outbound;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;

import java.util.List;

public class MetaEvent extends Event<PacketPlayOutEntityMetadata> {

    public final int id;
    public final List<DataWatcher.WatchableObject> metadata;

    public MetaEvent(HPlayer p, int id, List<DataWatcher.WatchableObject> metadata) {
        super(p);
        this.id = id;
        this.metadata = metadata;
    }
}