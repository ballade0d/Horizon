package xyz.hstudio.horizon.bukkit.network.events.outbound;

import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.WrappedPacket;
import xyz.hstudio.horizon.bukkit.util.Version;

import java.util.List;

public class MetaEvent extends Event {

    public final List<WatchableObject> objects;

    public MetaEvent(final HoriPlayer player, final List<WatchableObject> objects, final WrappedPacket packet) {
        super(player, packet);
        this.objects = objects;
    }

    @Override
    public void post() {
        if (Version.VERSION == Version.v1_8_R3) {
            for (WatchableObject object : this.objects) {
                if (object.index != 0) {
                    continue;
                }
                byte statue = (byte) object.object;
                if ((statue & 16) == 16) {
                    player.isEating = true;
                    player.isPullingBow = true;
                } else {
                    player.isEating = false;
                    player.isPullingBow = false;
                }
            }
        } else {
            for (WatchableObject object : this.objects) {
                if (object.index == 6) {
                    byte statue = (byte) object.object;
                    if ((statue & 1) == 1) {
                        player.isEating = true;
                        player.isPullingBow = true;
                    } else {
                        player.isEating = false;
                        player.isPullingBow = false;
                    }
                } else if (object.index == 0) {
                    byte statue = (byte) object.object;
                    player.isGliding = (statue & 128) == 128;
                }
            }
        }
    }

    @RequiredArgsConstructor
    public static class WatchableObject {
        public final int index;
        public final Object object;
    }
}