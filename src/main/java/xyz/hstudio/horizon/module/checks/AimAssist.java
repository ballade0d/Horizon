package xyz.hstudio.horizon.module.checks;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.linked.TDoubleLinkedList;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.wrapper.EntityWrapper;

@LoadFrom("checks/aim_assist.yml")
public class AimAssist extends CheckBase {

    @LoadInfo("enable")
    private static boolean ENABLE;

    private float lastDeltaPitch;
    private int lastHitTick;

    private float buffer;
    private EntityWrapper target;
    private final TDoubleList samples = new TDoubleLinkedList();

    public AimAssist(HPlayer p) {
        super(p, 1, 200, 200);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.teleport || Math.abs(e.to.pitch) == 90f || !e.hasLook) {
                return;
            }
            float deltaYaw = Math.abs(e.to.yaw - e.from.yaw);
            float deltaPitch = Math.abs(e.to.pitch - e.from.pitch);
            // common(e, deltaYaw);
            gcd(e, deltaPitch);

            lastDeltaPitch = deltaPitch;
        } else if (event instanceof EntityInteractEvent) {
            target = ((EntityInteractEvent) event).entity;
            lastHitTick = inst.getAsync().getTick();
        }
    }

    private void gcd(MoveEvent e, float deltaPitch) {
        if (deltaPitch == 0) {
            return;
        }

        float gcdPitch = MathUtils.gcd(deltaPitch, lastDeltaPitch);
        if (gcdPitch < 0.00001) {
            // System.out.println("Invalid GCD");
        }
    }

    private void common(MoveEvent e, float deltaYaw) {
        if (target == null || !e.hasPos || inst.getAsync().getTick() - lastHitTick > 20) {
            return;
        }

        Location origin = p.physics.position;
        Vector3D end = target.position();
        float optimalYaw = origin.setDirection(end.subtract(origin)).yaw % 360f;
        float rotationYaw = e.to.yaw;
        float fixedRotYaw = (rotationYaw % 360f + 360f) % 360f;
        double difference = Math.abs(fixedRotYaw - optimalYaw);
        if (deltaYaw > 3) {
            samples.add(difference);
        }

        if (samples.size() >= 25) {
            double[] data = samples.toArray();
            double avg = MathUtils.average(data);
            double deviation = MathUtils.stdev(data);
            if (avg < 8 && deviation < 12) {
                if (++buffer > 15) {
                    buffer = 10;
                    samples.remove(0, 4);
                    punish(e, "AimAssist (rTBhy)", 2, Detection.AIM_ASSIST, null);
                }
            } else buffer = Math.max(buffer - 1, 0);

            samples.removeAt(0);
        }
    }
}