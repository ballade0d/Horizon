package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.*;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.EntityBase;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Stream;

public class HitBox extends CheckBase {

    private static final int N = 5;
    private final float[] yaws = new float[N], pitches = new float[N];

    public HitBox(HPlayer p) {
        super(p, 1, 100, 100);
    }

    @Override
    public void received(InEvent event) {
        if (event instanceof MoveEvent) {
            move((MoveEvent) event);
        }
        if (event instanceof EntityInteractEvent) {
            EntityInteractEvent e = (EntityInteractEvent) event;
            if (e.getType() != EntityInteractEvent.InteractType.ATTACK) {
                return;
            }
            EntityBase entity = e.getEntity();

            int ping = AccessorBase.getInst().getPing(p);

            Vector3D headPos = p.physics().headPos();
            Vector3D direction = p.physics().position.getDirection();
            Ray3D ray = new Ray3D(headPos, direction);

            Stream<AABB> cubes = inst
                    .getAsync()
                    .getHistory(entity, ping, 2)
                    .stream()
                    .map(loc -> loc
                            .toAABB(entity.length(), entity.width())
                            .expand(0.1, 0.1, 0.1));

            Stream<Vector3D> intersections = cubes.map(cube -> cube.intersectsRay(ray, 0, Float.MAX_VALUE));

            OptionalDouble distance = intersections
                    .filter(Objects::nonNull)
                    .mapToDouble(vec -> vec.distance(headPos))
                    .min();

            if (distance.isPresent()) {
                System.out.println(distance.getAsDouble());
            }

            /*
            dirTest(e, inst
                    .getAsync()
                    .getHistory(entity, ping, 2)
                    .get(0)
                    .toAABB(entity.length(), entity.width())
                    .plus(-0.2, -0.1, -0.2, 0.2, 0.2, 0.2));
            */
        }
    }

    private void move(MoveEvent e) {
        if (!e.isHasLook()) {
            return;
        }
        float yaw = e.getTo().getYaw();
        float pitch = e.getTo().getPitch();
        // Remove the last value and add the new value to the head

        System.arraycopy(yaws, 0, yaws, 1, yaws.length - 1);
        yaws[0] = yaw;
        System.arraycopy(pitches, 0, pitches, 1, pitches.length - 1);
        pitches[0] = pitch;
    }

    private void dirTest(EntityInteractEvent e, AABB cube) {
        if (e.getType() != EntityInteractEvent.InteractType.ATTACK) {
            return;
        }

        Vector3D headPos = p.physics().headPos();

        Vector2D point = new Vector2D(yaws[0], pitches[0]);
        for (int i = 0; i < N - 1; i++) {
            Vector2D next = new Vector2D(yaws[i + 1], pitches[i + 1]);

            Ray2D ray2D = new Ray2D(point, next.minus(point));
            Ray2D.Tracer tracer2D = ray2D.new Tracer();

            double distance = point.distance(next);
            double rate = distance / 10;
            while (tracer2D.trace(rate) < distance) {
                Vector3D dir = MathUtils.getDirection((float) tracer2D.getX(), (float) tracer2D.getY());
                Ray3D ray3D = new Ray3D(headPos, dir);

                System.out.println(dir.getX() + " " + dir.getY() + " " + dir.getZ());

                ray3D.highlight(p.bukkit().getWorld(), 5, 0.1);

                if (cube.intersectsRay(ray3D, 0, Float.MAX_VALUE) != null) {
                    return;
                }

            }

            point = next;
        }

        System.out.println("No intersection");
    }
}