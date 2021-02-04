package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.LoadClass;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.*;
import xyz.hstudio.horizon.wrapper.EntityBase;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@LoadClass(file = "/checks/hit_box.yml")
public class HitBox extends CheckBase {

    @LoadInfo(path = "max_buffer")
    private static int max_buffer;
    @LoadInfo(path = "buffer_adder")
    private static double buffer_adder;
    @LoadInfo(path = "buffer_reducer")
    private static double buffer_reducer;
    @LoadInfo(path = "box_epsilon")
    private static double box_epsilon;
    @LoadInfo(path = "history_range")
    private static int history_range;
    @LoadInfo(path = "limit")
    private static double limit;
    @LoadInfo(path = "step")
    private static int step;

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

            double epsilon = entity.borderSize() + box_epsilon;

            List<AABB> cubes = inst
                    .getAsync()
                    .getHistory(entity, p.status.ping, history_range)
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
            if (dist > limit) {
                if ((buffer += buffer_adder) > max_buffer) {
                    punish(e, "HitBox (TYCqq)", (dist - limit) * 10, Detection.HIT_BOX,
                            "d:" + dist);
                }
            } else if (cubes.stream().noneMatch(this::direction)) {
                if ((buffer += buffer_adder) > max_buffer) {
                    punish(e, "HitBox (COY9Y)", 2, Detection.HIT_BOX, null);
                }
            } else {
                buffer = Math.max(buffer - buffer_reducer, 0);
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
            double rate = distance / step;

            // Check for the origin point
            Vector3D dir = MathUtils.getDirection(tracer2D.x, tracer2D.y);
            Ray3D ray3D = new Ray3D(headPos, dir);
            if (cube.intersectsRay(ray3D, 0, Float.MAX_VALUE) != null) {
                return true;
            }

            while (tracer2D.trace(rate) < distance) {
                dir = MathUtils.getDirection(tracer2D.x, tracer2D.y);
                ray3D = new Ray3D(headPos, dir);

                if (cube.intersectsRay(ray3D, 0, Float.MAX_VALUE) != null) {
                    return true;
                }
            }
            point = next;
        }
        return false;
    }
}