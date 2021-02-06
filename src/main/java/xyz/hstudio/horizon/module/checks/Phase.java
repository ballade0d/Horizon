package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.material.Openable;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.configuration.LoadFrom;
import xyz.hstudio.horizon.configuration.LoadInfo;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.*;
import xyz.hstudio.horizon.wrapper.BlockWrapper;
import xyz.hstudio.horizon.wrapper.WorldWrapper;

@LoadFrom("checks/phase.yml")
public class Phase extends CheckBase {

    @LoadInfo("enable")
    private static boolean ENABLE;

    private static final double HORIZONTAL_DISTANCE_THRESHOLD = Math.pow(0.4, 2);
    private static final double VERTICAL_DISTANCE_THRESHOLD = 1;

    public Phase(HPlayer p) {
        super(p, 1, 20, 20);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (!(event instanceof MoveEvent)) {
            return;
        }
        MoveEvent e = (MoveEvent) event;
        if (e.teleport) {
            return;
        }
        Location to = e.to;
        Location from = e.from;
        double distanceSquared = from.distanceSquared(to);
        if (distanceSquared == 0 || distanceSquared > 65) {
            return;
        }

        double horizDistanceSquared = Math.pow(to.x - from.x, 2) + Math.pow(to.z - from.z, 2);
        double vertDistance = Math.abs(to.y - from.y);

        AABB playerFrom = from.toAABB();
        playerFrom.shrink(0.1, 0, 0.1);
        playerFrom.min.y += 0.4;
        playerFrom.max.y -= 0.1;
        AABB playerTo = playerFrom.plus(e.velocity);

        Vector3D minBigBox = new Vector3D(Math.min(playerFrom.min.x, playerTo.min.x), Math.min(playerFrom.min.y, playerTo.min.y), Math.min(playerFrom.min.z, playerTo.min.z));
        Vector3D maxBigBox = new Vector3D(Math.max(playerFrom.max.x, playerTo.max.x), Math.max(playerFrom.max.y, playerTo.max.y), Math.max(playerFrom.max.z, playerTo.max.z));
        AABB bigBox = new AABB(minBigBox, maxBigBox);

        // We need to grab blocks below us too, such as fences
        AABB selection = bigBox.plus(0, -0.6, 0, 0, 0, 0);

        WorldWrapper world = to.world;
        for (int x = selection.min.getBlockX(); x <= selection.max.getBlockX(); x++) {
            for (int y = selection.min.getBlockY(); y <= selection.max.getBlockY(); y++) {
                for (int z = selection.min.getBlockZ(); z <= selection.max.getBlockZ(); z++) {
                    // Skip block if it updated within player AABB (only if they move slowly)
                    if (horizDistanceSquared <= HORIZONTAL_DISTANCE_THRESHOLD && vertDistance <= VERTICAL_DISTANCE_THRESHOLD) {
                        continue;
                    }

                    BlockWrapper block = world.getBlock(x, y, z);
                    if (block == null || !block.isSolid()) {
                        continue;
                    }
                    if (block.type() == Material.PISTON_MOVING_PIECE) {
                        continue;
                    }
                    if (Openable.class.isAssignableFrom(block.type().getData())) {
                        continue;
                    }
                    for (AABB test : block.boxes(p)) {
                        // check if "test" box is even in "bigBox"
                        if (!test.collides(bigBox)) {
                            continue;
                        }
                        boolean xCollide = collides2d(test.min.z, test.max.z, test.min.y, test.max.y, playerFrom.min.z, playerFrom.max.z, playerFrom.min.y, playerFrom.max.y, e.velocity.z, e.velocity.y);
                        boolean yCollide = collides2d(test.min.x, test.max.x, test.min.z, test.max.z, playerFrom.min.x, playerFrom.max.x, playerFrom.min.z, playerFrom.max.z, e.velocity.x, e.velocity.z);
                        boolean zCollide = collides2d(test.min.x, test.max.x, test.min.y, test.max.y, playerFrom.min.x, playerFrom.max.x, playerFrom.min.y, playerFrom.max.y, e.velocity.x, e.velocity.y);
                        if (xCollide && yCollide && zCollide) {
                            punish(e, "Phase ()", 1, Detection.PHASE, null);
                            return;
                        }
                    }
                }
            }
        }
    }

    // 2d collision test. check if hexagon collides with rectangle
    private boolean collides2d(double testMinX, double testMaxX, double testMinY, double testMaxY, double otherMinX, double otherMaxX, double otherMinY, double otherMaxY, double otherExtrudeX, double otherExtrudeY) {
        if (otherExtrudeX == 0) {
            return true; //prevent division by 0
        }
        double slope = otherExtrudeY / otherExtrudeX;
        double height;
        double height2;
        Vector2D lowerPoint;
        Vector2D upperPoint;
        if (otherExtrudeX > 0) { // extruding to the right
            height = -(slope * (otherExtrudeY > 0 ? otherMaxX : otherMinX)) + otherMinY;
            height2 = -(slope * (otherExtrudeY > 0 ? otherMinX : otherMaxX)) + otherMaxY;
            lowerPoint = new Vector2D((otherExtrudeY > 0 ? testMaxX : testMinX), testMinY);
            upperPoint = new Vector2D((otherExtrudeY > 0 ? testMinX : testMaxX), testMaxY);
        } else { // extruding to the left
            height = -(slope * (otherExtrudeY <= 0 ? otherMaxX : otherMinX)) + otherMinY;
            height2 = -(slope * (otherExtrudeY <= 0 ? otherMinX : otherMaxX)) + otherMaxY;
            lowerPoint = new Vector2D((otherExtrudeY <= 0 ? testMaxX : testMinX), testMinY);
            upperPoint = new Vector2D((otherExtrudeY <= 0 ? testMinX : testMaxX), testMaxY);
        }
        Line lowerLine = new Line(height, slope);
        Line upperLine = new Line(height2, slope);
        return lowerPoint.y <= upperLine.getYatX(lowerPoint.x) && upperPoint.y >= lowerLine.getYatX(upperPoint.x);
    }
}
