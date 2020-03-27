package xyz.hstudio.horizon.api.events.outbound;

import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.util.enums.Version;

import java.util.List;

public class MetaEvent extends Event {

    public final List<WatchableObject> objects;

    public MetaEvent(final HoriPlayer player, final List<WatchableObject> objects) {
        super(player);
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
                if ((statue & 16) != 16) {
                    player.isEating = false;
                    player.isPullingBow = false;
                    player.isBlocking = false;
                    break;
                }
            }
        } else if (Version.VERSION == Version.v1_12_R1) {
            for (WatchableObject object : this.objects) {
                if (object.index == 6) {
                    byte statue = (byte) object.object;
                    if ((statue & 1) != 1) {
                        player.isEating = false;
                        player.isPullingBow = false;
                        player.isBlocking = false;
                    }
                } else if (object.index == 0) {
                    byte statue = (byte) object.object;
                    player.isGliding = (statue & 128) == 128;
                }
            }
        } else {
            for (WatchableObject object : this.objects) {
                if (object.index == 7) {
                    byte statue = (byte) object.object;
                    if ((statue & 1) != 1) {
                        player.isEating = false;
                        player.isPullingBow = false;
                        player.isBlocking = false;
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