package xyz.hstudio.horizon.module.checks;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.*;
import xyz.hstudio.horizon.wrapper.EntityWrapper;

import java.util.*;

@LoadFrom("checks/hit_box.yml")
public class HitBox extends CheckBase {

    @LoadPath("enable")
    private static boolean ENABLE;
    @LoadPath("max_buffer")
    private static int MAX_BUFFER;
    @LoadPath("buffer_adder")
    private static double BUFFER_ADDER;
    @LoadPath("buffer_reducer")
    private static double BUFFER_REDUCER;
    @LoadPath("box_epsilon")
    private static double BOX_EPSILON;
    @LoadPath("history_range")
    private static int HISTORY_RANGE;
    @LoadPath("limit")
    private static double LIMIT;
    @LoadPath("step")
    private static int STEP;

    private final Deque<Vector2D> moves = new LinkedList<>();
    private double buffer;

    public HitBox(HPlayer p) {
        super(p, 1, 80, 80);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.hasLook) {
                return;
            }
            float yaw = e.to.yaw;
            float pitch = e.to.pitch;

            // Add the new value to the head
            moves.addFirst(new Vector2D(yaw, pitch));

            if (moves.size() > 5) {
                moves.removeLast();
            }
        } else if (event instanceof EntityInteractEvent) {
            EntityInteractEvent e = (EntityInteractEvent) event;
            if (e.type != EntityInteractEvent.InteractType.ATTACK) {
                return;
            }
            EntityWrapper entity = e.entity;
            if (entity == null) {
                return;
            }

            Vector3D headPos = p.physics.headPos();
            Vector3D direction = p.physics.position.getDirection();
            Ray3D ray = new Ray3D(headPos, direction);

            double epsilon = entity.borderSize() + BOX_EPSILON;

            List<AABB> cubes = new ArrayList<>(HISTORY_RANGE);

            for (Location history : inst.getAsync().getHistory(entity, p.status.ping, HISTORY_RANGE)) {
                cubes.add(history.toAABB(entity.length(), entity.width()).expand(epsilon, epsilon, epsilon));
            }

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
            if (dist > LIMIT && (buffer += BUFFER_ADDER) > MAX_BUFFER) {
                punish(e, "HitBox (TYCqq)", (dist - LIMIT) * 10, Detection.HIT_BOX,
                        "d:" + dist);
            } else if (cubes.stream().noneMatch(this::direction) && (buffer += BUFFER_ADDER) > MAX_BUFFER) {
                punish(e, "HitBox (COY9Y)", 2, Detection.HIT_BOX, null);
            } else {
                buffer = Math.max(buffer - BUFFER_REDUCER, 0);
            }
        }
    }

    private boolean direction(AABB cube) {
        if (moves.isEmpty()) {
            return true;
        }

        Vector3D headPos = p.physics.headPos();

        Iterator<Vector2D> iterator = moves.iterator();
        Vector2D previous = iterator.next();
        while (iterator.hasNext()) {
            Vector2D point = iterator.next();

            Ray2D currToPrev = new Ray2D(previous, point.minus(point));
            Ray2D.Tracer ctpTracer = currToPrev.new Tracer();

            double distance = previous.distance(point);
            double rate = distance / STEP;

            for (double traced = 0; traced < distance; traced += rate) {
                ctpTracer.trace(traced);

                Vector3D dir = MathUtils.getDirection(ctpTracer.x, ctpTracer.y);
                Ray3D ray3D = new Ray3D(headPos, dir);

                if (cube.intersectsRay(ray3D, 0, Float.MAX_VALUE) != null) {
                    return true;
                }
            }

            previous = point;
        }
        return false;
    }
}