package xyz.hstudio.horizon.module.checks;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.outbound.MetaEvent;
import xyz.hstudio.horizon.module.CheckBase;

import java.io.IOException;

public class HealthTag extends CheckBase {

    public HealthTag(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        if (event instanceof MetaEvent) {
            MetaEvent e = (MetaEvent) event;

            if (e.id == p.bukkit.getEntityId()) {
                return;
            }

            Entity entity = p.getWorld().getEntity(e.id);
            if (entity == null) {
                return;
            }
            if (entity instanceof EntityWither || entity instanceof EntityEnderDragon) {
                return;
            }
            if (!(entity instanceof EntityHuman) && !(entity instanceof EntityMonster) && !(entity instanceof EntityAnimal) && !(entity instanceof EntityGolem) && !(entity instanceof EntityWaterAnimal) && !(entity instanceof EntityVillager)) {
                return;
            }
            if (p.bukkit.getVehicle() != null && p.bukkit.getVehicle().getUniqueId().equals(entity.getUniqueID())) {
                return;
            }
            boolean reset = false;
            for (DataWatcher.WatchableObject object : e.metadata) {
                if (object.b() instanceof Float && object.c() == 3 && (float) object.b() > 0.0F) {
                    object.a(Float.NaN);
                    reset = true;
                    break;
                }
            }
            if (!reset) {
                return;
            }

            e.modify(packet -> {
                try {
                    PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
                    serializer.clear();
                    serializer.b(e.id);
                    DataWatcher.a(e.metadata, serializer);
                    packet.a(serializer);
                } catch (IOException ignore) {
                }
            });
        }
    }
}