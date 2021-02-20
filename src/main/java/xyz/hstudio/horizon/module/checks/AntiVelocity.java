package xyz.hstudio.horizon.module.checks;

import me.cgoo.api.cfg.LoadFrom;
import me.cgoo.api.cfg.LoadPath;
import org.bukkit.Material;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.Vector3D;

@LoadFrom("checks/anti_velocity.yml")
public class AntiVelocity extends CheckBase {

    @LoadPath("enable")
    private static boolean ENABLE;
    @LoadPath("xz_epsilon")
    public static double XZ_EPSILON;
    @LoadPath("y_epsilon")
    public static double Y_EPSILON;
    @LoadPath("restrict_midair_direction_changes")
    public static boolean RESTRICT_MIDAIR_DIRECTION_CHANGES;
    @LoadPath("min_speed_to_check")
    public static double MIN_SPEED_TO_CHECK;

    public AntiVelocity(HPlayer p) {
        super(p);
    }

    @Override
    public void run(Event<?> event) {
        if (!ENABLE) return;
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.teleport || p.nms.vehicle != null || p.status.isFlying) {
                return;
            }
            if (e.touchedBlocks.contains(Material.LADDER) || e.touchedBlocks.contains(Material.VINE)) {
                return;
            }
            if (e.failedVelocity) {
                punish(e, "AntiVelocity (uxzPm)", 1, Detection.ANTI_VELOCITY, null);
                return;
            }
            if (RESTRICT_MIDAIR_DIRECTION_CHANGES) {
                midair(e);
            }
        }
    }

    private void midair(MoveEvent e) {
        if (e.knockBack) {
            return;
        }

        boolean onGround = e.onGround; // um... is this safe?
        boolean wasOnGround = p.physics.onGround; //um... is this safe?

        if (!BlockUtils.blockNearbyIsSolid(e.to, true) && !wasOnGround && !onGround && !e.isTouchingBlocks() &&
                !BlockUtils.blockNearbyIsSolid(e.to.plus(0, 1, 0), true)) {

            Vector3D moveVector = e.velocity.newY(0);
            Vector3D prevVector = p.physics.velocity.newY(0);

            double angle = moveVector.angle(prevVector);
            double horizSpeedSquared = Math.pow(e.to.x - e.from.x, 2) + Math.pow(e.to.z - e.from.z, 2);
            double prevSpeed = p.status.hitSlowdown ? prevVector.length() * 0.6 : prevVector.length();

            double magnitudeThreshold = e.oldFriction * prevSpeed - 0.026001;

            // angle check
            if (horizSpeedSquared > MIN_SPEED_TO_CHECK && angle > 0.2) {
                punish(e, "AntiVelocity (paMPn)", 1, Detection.ANTI_VELOCITY, "a:" + angle);
            }
            // magnitude check
            else if (prevVector.lengthSquared() > 0.01 && moveVector.length() < magnitudeThreshold) {
                punish(e, "AntiVelocity (ej52K)", 1, Detection.ANTI_VELOCITY, null);
            }
        }
    }
}