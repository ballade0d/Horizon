package xyz.hstudio.horizon.module;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.util.*;
import xyz.hstudio.horizon.wrapper.EntityBase;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

public class HitBox extends CheckBase {

    private static final int N = 5;

    private final float[] yaws = new float[N], pitches = new float[N];
    private double buffer;

    public HitBox(HPlayer p) {
        super(p, 1, 80, 80);
    }

    @Override
    public void received(InEvent<?> event) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.hasLook) {
                return;
            }
            float yaw = e.to.yaw;
            float pitch = e.to.pitch;

            // Remove the last value and add the new value to the head
            System.arraycopy(yaws, 0, yaws, 1, yaws.length - 1);
            yaws[0] = yaw;
            System.arraycopy(pitches, 0, pitches, 1, pitches.length - 1);
            pitches[0] = pitch;
        } else if (event instanceof EntityInteractEvent) {
            EntityInteractEvent e = (EntityInteractEvent) event;
            if (e.type != EntityInteractEvent.InteractType.ATTACK) {
                return;
            }
            EntityBase entity = e.entity;
            if (entity == null) {
                return;
            }

            Vector3D headPos = p.physics.headPos();
            Vector3D direction = p.physics.position.getDirection();
            Ray3D ray = new Ray3D(headPos, direction);

            double epsilon = entity.borderSize() + 0.005;

            List<AABB> cubes = inst
                    .getAsync()
                    .getHistory(entity, p.status.ping, 3)
                    .stream().map(loc -> loc
                            .toAABB(entity.length(), entity.width())
                            .expand(epsilon, epsilon, epsilon))
                    .collect(Collectors.toList());

            if (cubes.isEmpty()) {
                return;
            }

            OptionalDouble distance = cubes
                    .stream()
                    .map(cube -> cube.intersectsRay(ray, 0, Float.MAX_VALUE))
                    .filter(Objects::nonNull)
                    .mapToDouble(vec -> vec.distance(headPos))
                    .min();

            double dist = distance.orElse(0);
            if (dist > 3.1) {
                if ((buffer += 1.5) > 3) {
                    System.out.println("Too far! Distance: " + dist);
                }
            } else if (cubes.stream().noneMatch(this::direction)) {
                if ((buffer += 1.5) > 3) {
                    System.out.println("Did not hit bounding box");
                }
            } else {
                buffer = Math.max(buffer - 0.75, 0);
            }
        }
    }

    private boolean direction(AABB cube) {
        Vector3D headPos = p.physics.headPos();

        Vector2D point = new Vector2D(yaws[0], pitches[0]);
        for (int i = 0; i < N - 1; i++) {
            if (point.x == 0 && point.y == 0) {
                return true;
            }

            Vector2D next = new Vector2D(yaws[i + 1], pitches[i + 1]);

            Ray2D ray2D = new Ray2D(point, next.minus(point));
            Ray2D.Tracer tracer2D = ray2D.new Tracer();

            double distance = point.distance(next);
            double rate = distance / 10;
            while (tracer2D.trace(rate) < distance) {
                Vector3D dir = MathUtils.getDirection(tracer2D.x, tracer2D.y);
                Ray3D ray3D = new Ray3D(headPos, dir);

                if (cube.intersectsRay(ray3D, 0, Float.MAX_VALUE) != null) {
                    return true;
                }
            }
            point = next;
        }
        return false;
    }
}