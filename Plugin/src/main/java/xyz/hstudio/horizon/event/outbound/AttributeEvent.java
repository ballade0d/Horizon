package xyz.hstudio.horizon.event.outbound;

import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateAttributes;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.OutEvent;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AttributeEvent extends OutEvent<PacketPlayOutUpdateAttributes> {

    public static final UUID SPRINT_UUID = UUID.fromString("662a6b8d-da3e-4c1c-8813-96ea6097278d");
    public static final AttributeModifier SPRINT_MODIFIER = new AttributeModifier(SPRINT_UUID, 0.3, 2);

    public final List<AttributeSnapshot> snapshots;

    public AttributeEvent(HPlayer player, List<AttributeSnapshot> snapshots) {
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
                if (!p.status.isSprinting) {
                    // p.speedData.attributeBypass = true;
                    // player.sendSimulatedAction(() -> player.speedData.attributeBypass = false);

                    p.sendSimulatedAction(p.moveFactors::clear);
                }
            } else {
                snapshot.modifiers.sort(Comparator.comparingInt(o -> o.operation));

                // This rewrites the whole list
                p.sendSimulatedAction(() -> {
                    p.moveFactors.clear();
                    p.moveFactors.addAll(snapshot.modifiers);
                });
            }
            break;
        }
    }

    @AllArgsConstructor
    public static class AttributeSnapshot {
        public final String key;
        public final double baseValue;
        public final List<AttributeModifier> modifiers;
    }

    @AllArgsConstructor
    public static class AttributeModifier {
        public final UUID uuid;
        public final double value;
        public final int operation;
    }
}