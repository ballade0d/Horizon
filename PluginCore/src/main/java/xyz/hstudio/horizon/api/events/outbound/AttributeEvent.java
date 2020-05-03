package xyz.hstudio.horizon.api.events.outbound;

import lombok.AllArgsConstructor;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AttributeEvent extends Event {

    public static final UUID SPRINT_UUID = UUID.fromString("662a6b8d-da3e-4c1c-8813-96ea6097278d");

    public final List<AttributeSnapshot> snapshots;

    public AttributeEvent(final HoriPlayer player, final List<AttributeSnapshot> snapshots) {
        super(player);
        this.snapshots = snapshots;
    }

    @Override
    public void post() {
        for (AttributeSnapshot snapshot : this.snapshots) {
            if (!"generic.movementSpeed".equals(snapshot.key)) {
                continue;
            }
            if (snapshot.modifiers.size() == 0) {
                if (!player.isSprinting) {
                    player.speedData.attributeBypass = true;
                    player.sendSimulatedAction(() -> player.speedData.attributeBypass = false);
                    player.moveModifiers.clear();
                }
                break;
            }
            snapshot.modifiers.sort(Comparator.comparingInt(o -> o.operation));
            player.moveModifiers = snapshot.modifiers;
        }
    }

    @AllArgsConstructor
    public static class AttributeSnapshot {
        public final String key;
        public final double baseValue;
        public final int size;
        public final List<AttributeModifier> modifiers;
    }

    @AllArgsConstructor
    public static class AttributeModifier {
        public final UUID uuid;
        public final double value;
        public final int operation;
    }
}