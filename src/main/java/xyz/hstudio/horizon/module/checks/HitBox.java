package xyz.hstudio.horizon.module.checks;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import me.cgoo.api.util.Pair;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.*;
import xyz.hstudio.horizon.wrapper.BlockWrapper;
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
            if (!e.hasLook && !moves.isEmpty()) {
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

            double epsilon = entity.borderSize() + BOX_EPSILON;

            List<AABB> cubes = new ArrayList<>(HISTORY_RANGE);

            for (Location history : inst.getAsync().getHistory(entity, p.status.ping, HISTORY_RANGE)) {
                cubes.add(history.toAABB(entity.length(), entity.width()).expand(epsilon, epsilon, epsilon));
            }
            if (cubes.isEmpty() || moves.isEmpty()) {
                return;
            }

            Vector3D headPos = p.physics.headPos();
            Vector3D dir = p.physics.position.getDirection();

            List<Pair<Vector3D, Double>> solutions = findSolutions(cubes, headPos);

            if (solutions.isEmpty()) {
                if ((buffer += BUFFER_ADDER) > MAX_BUFFER) {
                    punish(e, "HitBox (COY9Y)", 1, Detection.HIT_BOX, null);
                }
                return;
            }

            solutions.sort(Comparator.comparingDouble(o -> o.getKey().angle(dir)));

            Vector3D bestDir = solutions.get(0).getKey();
            double distance = solutions.get(0).getValue();

            Ray3D ray3D = new Ray3D(headPos, bestDir);
            Ray3D.Tracer tracer = ray3D.new Tracer();

            for (double traced = 0; traced < distance; traced += 0.1) {
                Vector3D point = new Vector3D(NumberConversions.floor(tracer.x),
                        NumberConversions.floor(tracer.y), NumberConversions.floor(tracer.z));
                BlockWrapper block = entity.world.getBlock(point);
                if (block != null && BlockUtils.isSolid(block) &&
                        Arrays.stream(block.boxes(p)).anyMatch(box -> box.collides(point))) {
                    punish(e, "HitBox (11mr0)", 1, Detection.HIT_BOX, "t:" + block.type());
                    return;
                }

                tracer.trace(0.1);
            }

            buffer = Math.max(buffer - BUFFER_REDUCER, 0);
        }
    }

    private List<Pair<Vector3D, Double>> findSolutions(List<AABB> cubes, Vector3D headPos) {
        List<Pair<Vector3D, Double>> solutions = new ArrayList<>(HISTORY_RANGE);

        for (AABB cube : cubes) {
            Iterator<Vector2D> iterator = moves.iterator();
            Vector2D previous = iterator.next();

            while (iterator.hasNext()) {
                Vector2D next = iterator.next();

                Ray2D currToPrev = new Ray2D(previous, next.clone().subtract(next));
                Ray2D.Tracer currToPrevTracer = currToPrev.new Tracer();

                double distToTrace = previous.distance(next);
                double rate = distToTrace / STEP;

                for (double traced = 0; traced < distToTrace; traced += rate) {
                    currToPrevTracer.trace(rate);

                    Vector3D currToPrevDir = MathUtils.getDirection(currToPrevTracer.x, currToPrevTracer.y);
                    Ray3D ray3D = new Ray3D(headPos, currToPrevDir);

                    Vector3D result = cube.intersectsRay(ray3D, 0, Float.MAX_VALUE);
                    double distance;

                    if (result == null || (distance = headPos.distance(result)) > LIMIT) {
                        continue;
                    }

                    solutions.add(new Pair<>(currToPrevDir, distance));
                }

                previous = next;
            }
        }

        return solutions;
    }
}