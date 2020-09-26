package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.EntityBase;

public class HitBox extends CheckBase {

    public HitBox(HPlayer p, int decayAmount, int decayDelay, int decayInterval) {
        super(p, decayAmount, decayDelay, decayInterval);
    }

    @Override
    public void received(InEvent event) {
        if (event instanceof EntityInteractEvent) {
            EntityInteractEvent e = (EntityInteractEvent) event;
            if (e.getType() != EntityInteractEvent.InteractType.ATTACK) {
                return;
            }
            EntityBase entity = e.getEntity();

            Vector3D origin = p.getPhysics().getPosition();
            int ping = 1; // TODO: Finish this

            double width = Math.hypot(entity.width(), entity.width()) / 2;

            double distance = inst.getAsync()
                    .getHistory(entity, ping, 1)
                    .stream()
                    .mapToDouble(origin::distance)
                    .min()
                    .orElse(-1);
            distance -= width;

            if (distance > 3) {
                //
            }
        }
    }
}