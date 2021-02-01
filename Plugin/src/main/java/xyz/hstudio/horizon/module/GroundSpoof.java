package xyz.hstudio.horizon.module;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;

public class GroundSpoof extends CheckBase {

    public GroundSpoof(HPlayer p) {
        super(p, 1, 40, 40);
    }

    @Override
    public void received(InEvent<?> event) {
        if (event instanceof MoveEvent) {
            check((MoveEvent) event);
        }
    }

    private void check(MoveEvent e) {
        if (e.onGroundReally || e.step || e.teleport) {
            return;
        }
        /*
        if (player.currentTick < 20 || player.vehicleBypass ||
                e.piston || !e.collidingEntities.isEmpty() || player.isFlying()) {
            return
        }
         */
        if (e.onGround) {
            // Must also check position before, because in the client, Y is clipped first.
            // In the client, if Y is clipped, then onGround is set to true.
            Location checkLoc = e.from.newY(e.to.y);

            // Stop checker-climbers
            AABB aabb = e.to.toAABB();
            aabb.expand(0, -0.0001, 0);

            boolean notPhasing = aabb.getBlockAABBs(p, p.getWorld(), Material.WEB).isEmpty();
            boolean pass = checkLoc.onGround(p, false, 0.02);
            if (notPhasing && pass) {
                return;
            }

            // TODO: Check for client blocks

            e.modify(packet -> {
                try {
                    PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
                    packet.b(serializer);
                    if (packet instanceof PacketPlayInFlying.PacketPlayInPosition) {
                        serializer.setByte(24, 0);
                    } else if (packet instanceof PacketPlayInFlying.PacketPlayInLook) {
                        serializer.setByte(8, 0);
                    } else if (packet instanceof PacketPlayInFlying.PacketPlayInPositionLook) {
                        serializer.setByte(32, 0);
                    }
                } catch (Exception ignore) {
                }
            });

            System.out.println("GroundSpoof");
        }
    }
}
