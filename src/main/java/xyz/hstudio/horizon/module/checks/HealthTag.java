package xyz.hstudio.horizon.module.checks;

import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.outbound.MetaEvent;
import xyz.hstudio.horizon.module.CheckBase;

import java.io.IOException;

@LoadFrom("checks/health_tag.yml")
public class HealthTag extends CheckBase {

    @LoadInfo("enable")
    private static boolean ENABLE;

    public HealthTag(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MetaEvent) {
            MetaEvent e = (MetaEvent) event;

            if (e.id == p.nms.getId()) {
                return;
            }

            Entity entity = p.world().getEntity(e.id);
            if (entity == null) {
                return;
            }
            if (entity instanceof EntityWither || entity instanceof EntityEnderDragon) {
                return;
            }
            if (!(entity instanceof EntityHuman) && !(entity instanceof EntityMonster) && !(entity instanceof EntityAnimal) && !(entity instanceof EntityGolem) && !(entity instanceof EntityWaterAnimal) && !(entity instanceof EntityVillager)) {
                return;
            }
            if (entity.equals(p.nms.vehicle)) {
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

            e.setCancelled(true);
            try {
                PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
                serializer.b(e.id);
                DataWatcher.a(e.metadata, serializer);
                Packet<?> packet = new PacketPlayOutEntityMetadata();
                packet.a(serializer);
                p.pipeline.writeAndFlush(packet);
            } catch (IOException ignore) {
            }
        }
    }
}