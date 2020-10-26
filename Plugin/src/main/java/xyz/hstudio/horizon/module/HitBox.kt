package xyz.hstudio.horizon.module

import xyz.hstudio.horizon.HPlayer
import xyz.hstudio.horizon.event.InEvent
import xyz.hstudio.horizon.event.inbound.EntityInteractEvent
import xyz.hstudio.horizon.event.inbound.MoveEvent
import xyz.hstudio.horizon.util.*
import java.util.*
import java.util.stream.Collectors

class HitBox(p: HPlayer) : CheckBase(p, 1, 80, 80) {

    private val yaws = FloatArray(N)
    private val pitches = FloatArray(N)
    private var buffer = 0.0

    override fun received(event: InEvent?) {
        if (event is MoveEvent) {
            move(event)
        } else if (event is EntityInteractEvent) {
            interact(event)
        }
    }

    private fun move(e: MoveEvent) {
        if (!e.hasLook) {
            return
        }
        val yaw = e.to.yaw
        val pitch = e.to.pitch

        // Remove the last value and add the new value to the head
        System.arraycopy(yaws, 0, yaws, 1, yaws.size - 1)
        yaws[0] = yaw
        System.arraycopy(pitches, 0, pitches, 1, pitches.size - 1)
        pitches[0] = pitch
    }

    private fun interact(e: EntityInteractEvent) {
        if (e.type != EntityInteractEvent.InteractType.ATTACK) {
            return
        }
        val entity = e.entity
        val headPos = p.physics.headPos()
        val direction = p.physics.position.direction
        val ray = Ray3D(headPos, direction)

        if (entity == null) {
            return; //to avoid NPE
        }

        val epsilon = entity.borderSize() + 0.005
        val cubes = inst
                .async
                .getHistory(entity, p.status.ping, 3)
                .stream().map { loc: Location ->
                    loc
                            .toAABB(entity.length().toDouble(), entity.width().toDouble())
                            .expand(epsilon, epsilon, epsilon)
                }
                .collect(Collectors.toList())
        if (cubes.isEmpty()) {
            return
        }
        val distance = cubes
                .stream()
                .map { cube: AABB -> cube.intersectsRay(ray, 0f, Float.MAX_VALUE) }
                .filter { obj: Vector3D? -> Objects.nonNull(obj) }
                .mapToDouble { vec: Vector3D -> vec.distance(headPos) }
                .min()
        val dist = distance.orElse(0.0)
        if (dist > 3.1) {
            if (1.5.let { buffer += it; buffer } > 3) {
                println("Too far! Distance: $dist")
            }
        } else if (cubes.stream().noneMatch { cube: AABB -> direction(cube) }) {
            if (1.5.let { buffer += it; buffer } > 3) {
                println("Did not hit bounding box")
            }
        } else {
            buffer = (buffer - 0.75).coerceAtLeast(0.0)
        }
    }

    private fun direction(cube: AABB): Boolean {
        val headPos = p.physics.headPos()
        var point = Vector2D(yaws[0].toDouble(), pitches[0].toDouble())
        for (i in 0 until N - 1) {
            if (point.x == 0.0 && point.y == 0.0) {
                return true
            }
            val next = Vector2D(yaws[i + 1].toDouble(), pitches[i + 1].toDouble())
            val ray2D = Ray2D(point, next.minus(point))
            val tracer2D = ray2D.Tracer()
            val distance = point.distance(next)
            val rate = distance / 10
            while (tracer2D.trace(rate) < distance) {
                val dir = MathUtils.getDirection(tracer2D.x.toFloat(), tracer2D.y.toFloat())
                val ray3D = Ray3D(headPos, dir)
                if (cube.intersectsRay(ray3D, 0f, Float.MAX_VALUE) != null) {
                    return true
                }
            }
            point = next
        }
        return false
    }

    companion object {
        private const val N = 5
    }
}