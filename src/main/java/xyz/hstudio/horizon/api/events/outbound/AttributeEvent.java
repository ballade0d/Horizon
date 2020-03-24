package xyz.hstudio.horizon.api.events.outbound;

import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.data.HoriPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AttributeEvent extends Event {

    private static final UUID SPRINT_UUID = UUID.fromString("662a6b8d-da3e-4c1c-8813-96ea6097278d");

    public final List<AttributeSnapshot> snapshots;

    public AttributeEvent(final HoriPlayer player, final List<AttributeSnapshot> snapshots) {
        super(player);
        this.snapshots = snapshots;
    }

    @Override
    public void post() {
        for (AttributeSnapshot snapshot : this.snapshots) {
            if (!snapshot.key.equals("generic.movementSpeed")) {
                continue;
            }
            if (snapshot.modifiers.size() == 0 && !player.isSprinting) {
                player.moveFactor = (float) snapshot.baseValue;
                break;
            }
            snapshot.modifiers.sort(Comparator.comparingInt(o -> o.operation));
            for (AttributeModifier modifier : snapshot.modifiers) {
                if (modifier.uuid.equals(SPRINT_UUID)) {
                    break;
                }
                switch (modifier.operation) {
                    case 0:
                        player.moveFactor += modifier.value;
                        break;
                    case 1:
                    case 2:
                        // Idk what operation 1 do, figure it out sometime.
                        player.moveFactor += player.moveFactor * modifier.value;
                        break;
                }
            }
            break;
        }
    }

    @RequiredArgsConstructor
    public static class AttributeSnapshot {
        public final String key;
        public final double baseValue;
        public final int size;
        public final List<AttributeModifier> modifiers;
    }

    @RequiredArgsConstructor
    public static class AttributeModifier {
        public final UUID uuid;
        public final double value;
        public final int operation;
    }
}