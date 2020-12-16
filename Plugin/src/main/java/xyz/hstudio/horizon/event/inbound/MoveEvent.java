package xyz.hstudio.horizon.event.inbound;

import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.event.InEvent;
import xyz.hstudio.horizon.util.AABB;
import xyz.hstudio.horizon.util.Location;
import xyz.hstudio.horizon.util.Pair;
import xyz.hstudio.horizon.util.Vector3D;
import xyz.hstudio.horizon.util.enums.Direction;
import xyz.hstudio.horizon.wrapper.BlockBase;

import java.util.List;
import java.util.Set;

import static xyz.hstudio.horizon.util.Physics.AIR_RESISTANCE_VERTICAL;
import static xyz.hstudio.horizon.util.Physics.GRAVITATIONAL_ACCELERATION;

public class MoveEvent extends InEvent<PacketPlayInFlying> {

    public final Location to;
    public final boolean onGround;
    public final boolean hasLook;
    public final boolean hasPos;

    public Vector3D velocity;
    public Set<Direction> touchFaces;
    public Set<Material> touchBlocks;
    public boolean knockBack;
    public boolean teleport;
    public boolean onGroundReally;
    public boolean touchCeiling;
    public boolean step;
    public boolean jump;
    public boolean failedVelocity;
    public Vector3D acceptedVelocity;
    // TODO: Finish this
    //This is the friction that is used to compute this move's initial force.
    private float newFriction;
    //This is the friction that affects this move's velocity.
    private float oldFriction;

    public MoveEvent(HPlayer p, Location to, boolean onGround, boolean hasLook, boolean hasPos) {
        super(p);
        this.to = to;
        this.onGround = onGround;
        this.hasLook = hasLook;
        this.hasPos = hasPos;
    }

    @Override
    public boolean pre() {
        this.velocity = to.minus(p.physics.position);

        this.newFriction = computeFriction();
        this.oldFriction = p.physics.friction;

        this.touchFaces = new AABB(to.x - 0.299999, to.y + 0.000001, to.z - 0.299999, to.x + 0.299999, to.y + 1.799999, to.z + 0.299999).touchingFaces(p, to.world, 0.0001);
        this.touchBlocks = AABB.player().expand(-0.001, -0.001, -0.001).add(velocity).getMaterials(to.world);

        /*
        switch (this.key) {
            case W:
                forward = 1f;
                break;
            case A:
                strafe = 1f;
                break;
            case S:
                forward = -1f;
                break;
            case D:
                strafe = -1f;
                break;
            case W_A:
                forward = strafe = 1f;
                break;
            case W_D:
                forward = 1f;
                strafe = -1f;
                break;
            case S_A:
                forward = -1f;
                strafe = 1f;
                break;
            case S_D:
                forward = strafe = -1f;
                break;
            default:
                break;
        }

        if (p.velocity.x != 0 || p.velocity.y != 0 || p.velocity.z != 0) {
            float xVel = p.velocity.x;
            float zVel = p.velocity.z;

            if (p.status.hitSlowdown) {
                xVel *= 0.6;
                zVel *= 0.6;
            }

            float friction = p.physics.onGround ?
                    p.moveFactor() * 0.16277136f / (oldFriction * oldFriction * oldFriction) :
                    p.status.isSprinting ? 0.026f : 0.02f;

            if (p.status.isEating || p.status.isBlocking || p.status.isPullingBow) {
                forward *= 0.2f;
                strafe *= 0.2f;
            }

            double f = strafe * strafe + forward * forward;
            if (f >= 1.0E-4f) {
                f = Math.sqrt(f);
                if (f < 1) {
                    f = 1;
                }
                f = friction / f;
                strafe *= f;
                forward *= f;
                double f1 = MathHelper.sin(to.yaw * (float) Math.PI / 180f);
                double f2 = MathHelper.cos(to.yaw * (float) Math.PI / 180f);
                xVel += strafe * f2 - forward * f1;
                zVel += forward * f2 + strafe * f1;
            }

            this.expectedVelocity = new Vector3D(xVel, p.velocity.y, zVel);

            boolean spec = touchFaces.contains(Direction.UP) || touchBlocks.contains(Material.LADDER) ||
                    touchBlocks.contains(Material.VINE) || touchBlocks.contains(Material.WEB) ||
                    touchFaces.stream().anyMatch(Direction::horizontal);

            double ratio = velocity.length() / expectedVelocity.length();
            this.knockBack = spec || ratio > 0.8;

            if (knockBack) {
                this.acceptedVelocity = velocity;
                p.velocity.y = 0;
            }
            p.velocity.x = xVel * friction;
            p.velocity.z = zVel * friction;
            if (System.currentTimeMillis() - p.velocity.time > 300L) {
                p.velocity.x = p.velocity.z = 0;
            }
            if (Math.abs(p.velocity.x) < 0.005) {
                p.velocity.x = 0;
            }
            if (Math.abs(p.velocity.z) < 0.005) {
                p.velocity.z = 0;
            }
        }
        */

        if (p.status.isTeleporting) {
            Location tpLoc;
            long elapsedTime;
            if (p.teleports.size() == 0) {
                tpLoc = null;
                elapsedTime = 0;
            } else {
                Pair<Location, Long> tpPair = p.teleports.get(0);
                tpLoc = tpPair.getKey();
                elapsedTime = System.currentTimeMillis() - tpPair.getValue();
            }

            if (!onGround && hasPos && hasLook && to.equals(tpLoc)) {
                p.physics.position = tpLoc;
                p.physics.velocity = new Vector3D(0, 0, 0);

                p.teleports.remove(0);

                this.teleport = true;
                inst.getAsync().clearHistory(p.base);

                if (p.teleports.size() == 0) {
                    p.status.isTeleporting = false;
                } else {
                    return false;
                }
            } else if (!p.bukkit.isSleeping()) {
                if (elapsedTime > p.status.ping + 800) {
                    Location tp;
                    if (p.teleports.size() > 0) {
                        tp = p.teleports.get(p.teleports.size() - 1).getKey();
                        p.teleports.clear();
                    } else tp = p.physics.position;
                    inst.getSync().teleport(p, tp);
                }
            }
        }
        this.touchCeiling = testTouchCeiling();
        this.onGroundReally = to.isOnGround(p, false, 0.001);
        this.step = testStep();
        this.jump = testJump();

        this.acceptedVelocity = testVelocity();
        this.knockBack = this.acceptedVelocity != null;

        return true;
    }

    private float computeFriction() {
        float friction = 0.91F;
        if (onGround) {
            Vector3D pos = p.physics.position;
            BlockBase b = new Location(to.world, pos.x, pos.y - 1, pos.z).getBlock();
            if (b != null) {
                friction *= b.friction();
            }
        }
        return friction;
    }

    /*
    private Key computeKey() {
        if ((p.physics.onGround && !onGround) || (velocity.x == 0.0 && velocity.z == 0.0 && onGround)) {
            return p.physics.key;
        }
        double prevX;
        double prevZ;
        if (p.velocity.firstTick) {
            prevX = p.velocity.x;
            prevZ = p.velocity.z;
        } else {
            prevX = p.physics.velocity.x;
            prevZ = p.physics.velocity.z;
            if (p.physics.wasOnGround) {
                prevX *= oldFriction;
                prevZ *= oldFriction;
            } else {
                prevX *= 0.91;
                prevZ *= 0.91;
            }
            if (Math.abs(prevX) < 0.005) {
                prevX = 0;
            }
            if (Math.abs(prevZ) < 0.005) {
                prevZ = 0;
            }
        }

        if (p.status.hitSlowdown) {
            prevX *= 0.6;
            prevZ *= 0.6;
        }

        double accelX = velocity.x - prevX;
        double accelZ = velocity.z - prevZ;

        System.out.println(velocity.x + ", "+velocity.z + ", "+accelX + ", "+accelZ);

        int precision = String.valueOf((int) Math.abs(Math.max(to.x, to.z))).length();
        precision = 15 - precision;
        double preD = 1.2 * Math.pow(10, -Math.max(3, precision - 5));

        Key key = Key.NONE;
        if (Math.abs(accelX) + Math.abs(accelZ) > preD) {
            float motionYaw = (float) (Math.toDegrees(Math.atan2(accelZ, accelX)) - 90 - to.yaw);
            while (motionYaw > 360) {
                motionYaw -= 360;
            }
            while (motionYaw < 0) {
                motionYaw += 360;
            }
            motionYaw /= 45f;
            int dir = NumberConversions.round(motionYaw);
            if (dir == 0 || dir == 8) {
                key = Key.W;
            } else if (dir == 1) {
                key = Key.W_D;
            } else if (dir == 2) {
                key = Key.D;
            } else if (dir == 3) {
                key = Key.S_D;
            } else if (dir == 4) {
                key = Key.S;
            } else if (dir == 5) {
                key = Key.S_A;
            } else if (dir == 6) {
                key = Key.A;
            } else if (dir == 7) {
                key = Key.W_A;
            }
        }

        return key;
    }

    // Wrong condition when player stop pressing any key.
    // Wrong condition when jumping
    private Key computeKey() {
        Vector3D prev = new Vector3D(p.physics.velocity.x, 0, p.physics.velocity.z);

        if (p.status.hitSlowdown) {
            prev.multiply(0.6);
        }

        for (Material material : touchBlocks) {
            if (material == Material.SOUL_SAND) {
                prev.multiply(0.4);
            }
            if (material == Material.WEB) {
                prev.multiply(0);
                // Any number times 0 is 0, so no reason to continue looping.
                break;
            }
        }

        if (Math.abs(prev.x * oldFriction) < 0.005) {
            prev.x = 0;
        }
        if (Math.abs(prev.z * oldFriction) < 0.005) {
            prev.z = 0;
        }
        double dX = velocity.x;
        double dZ = velocity.z;
        System.out.println(dX + ", " + dZ);
        dX /= p.velocity.firstTick && !p.physics.onGround ? 1 : oldFriction;
        dZ /= p.velocity.firstTick && !p.physics.onGround ? 1 : oldFriction;
        dX -= p.velocity.firstTick ? p.velocity.x : prev.x;
        dZ -= p.velocity.firstTick ? p.velocity.z : prev.z;

        if(Math.abs(dX) < 1E-6 || Math.abs(dZ) < 1E-6){
            return Key.NONE;
        }

        Vector3D accelDir = new Vector3D(dX, 0, dZ).normalize();
        Vector3D yaw = MathUtils.getDirection(to.yaw, 0).normalize();

        double angle = Math.toDegrees(yaw.angle(accelDir));

        double cross = yaw.z * accelDir.x - accelDir.z * yaw.x;

        Key key = Key.NONE;
        if (Math.abs(angle - 0) < 1) {
            key = Key.W;
        } else if (Math.abs(angle - 45) < 1) {
            key = cross > 0 ? Key.W_A : Key.W_D;
        } else if (Math.abs(angle - 90) < 1) {
            key = cross > 0 ? Key.A : Key.D;
        } else if (Math.abs(angle - 135) < 1) {
            key = cross > 0 ? Key.S_A : Key.S_D;
        } else if (Math.abs(angle - 180) < 1) {
            key = Key.S;
        }

        System.out.println(key + ", " + angle);

        return key;
    }
    */

    private boolean testTouchCeiling() {
        Vector3D pos = p.physics.position.newY(to.y);
        AABB collisionBox = AABB.player().expand(-0.000001, -0.000001, -0.000001).add(pos);
        return collisionBox.touchingFaces(p, to.world, 0.0001).contains(Direction.UP);
    }

    private boolean testStep() {
        Vector3D prevPos = p.physics.position;
        Location extrapolate = to;
        // when on ground, Y velocity is inherently 0; no need to do pointless math.
        extrapolate.newY(extrapolate.y + (p.physics.onGroundReally ? -0.0784 :
                ((p.physics.velocity.y + GRAVITATIONAL_ACCELERATION) * AIR_RESISTANCE_VERTICAL)));

        AABB box = AABB.player().add(extrapolate);
        List<AABB> verticalCollision = box.getBlockAABBs(p, p.getWorld(), Material.WEB);

        if (verticalCollision.isEmpty() && !p.physics.onGround) return false;

        double highestVertical = extrapolate.y;
        for (AABB blockAABB : verticalCollision) {
            double aabbMaxY = blockAABB.max.y;
            if (aabbMaxY > highestVertical) highestVertical = aabbMaxY;
        }

        // move to this position, but with clipped Y (moving horizontally)
        box = AABB.player().add(to.newY(highestVertical)).expand(0, -0.00000000001, 0);

        List<AABB> horizontalCollision = box.getBlockAABBs(p, p.getWorld(), Material.WEB);

        if (horizontalCollision.isEmpty()) return false;

        double expectedY = prevPos.y;
        double highestPointOnAABB = -1;
        for (AABB blockAABB : horizontalCollision) {
            double blockAABBY = blockAABB.max.y;
            if (blockAABBY - prevPos.y > 0.6) {
                return false;
            }
            if (blockAABBY > expectedY) {
                expectedY = blockAABBY;
            }
            if (blockAABBY > highestPointOnAABB) {
                highestPointOnAABB = blockAABBY;
            }
        }

        return (onGround || onGroundReally) && Math.abs(prevPos.y - highestPointOnAABB) > 0.0001 &&
                Math.abs(to.y - expectedY) < 0.0001;
    }

    // Checks if the player's dY matches the expected dY
    private boolean testJump() {
        int jumpBoostLvl = p.getPotionAmplifier(PotionEffectType.JUMP);
        float expectedDY = Math.max(0.42f + jumpBoostLvl * 0.1f, 0f);
        boolean leftGround = p.physics.onGround && !onGround;
        float dY = (float) (to.y - p.physics.position.y);

        // Jumping right as you enter a 2-block-high space will not change your motY.
        // When these conditions are met, we'll give them the benefit of the doubt and say that they jumped.
        {
            AABB box = AABB.player();
            box.expand(-0.000001, -0.000001, -0.000001);
            box.add(to.plus(0, expectedDY, 0));
            boolean collidedNow = !box.getBlockAABBs(p, to.world).isEmpty();

            box = AABB.player();
            box.expand(-0.000001, -0.000001, -0.000001);
            box.add(p.physics.position.plus(0, expectedDY, 0));
            boolean collidedBefore = !box.getBlockAABBs(p, to.world).isEmpty();

            if (collidedNow && !collidedBefore && leftGround && dY == 0) {
                expectedDY = 0;
            }
        }

        Set<Material> touchedBlocks = p.base.cube(to).getMaterials(p.getWorld());
        if (touchedBlocks.contains(Material.WEB)) {
            if (hasPos) {
                expectedDY *= 0.05;
            } else {
                expectedDY = 0;
            }
        }

        boolean kbSimilarToJump = acceptedVelocity != null &&
                (Math.abs(acceptedVelocity.y - expectedDY) < 0.001 || touchCeiling);
        return !kbSimilarToJump &&
                ((expectedDY == 0 && p.physics.onGround) || leftGround) &&
                (dY == expectedDY || touchCeiling);
    }

    private Vector3D testVelocity() {
        if (p.velocity.x == 0 && p.velocity.y == 0 && p.velocity.z == 0) {
            return null;
        }
        double speedPotMultiplier = 1 + p.getPotionAmplifier(PotionEffectType.SPEED) * 0.2;
        double sprintMultiplier = p.status.isSprinting ? 1.3 : 1;
        double weirdConstant = jump && p.status.isSprinting ? 0.2518462 : 0.098;
        double baseMultiplier = 5 * p.bukkit.getWalkSpeed() * speedPotMultiplier;
        double maxDiscrepancy = weirdConstant * baseMultiplier * sprintMultiplier + 0.003;

        double x = p.status.hitSlowdown ? 0.6 * p.velocity.x : p.velocity.x;
        double y = p.velocity.y;
        double z = p.status.hitSlowdown ? 0.6 * p.velocity.z : p.velocity.z;

        if (!((touchFaces.contains(Direction.UP) && y > 0) || (touchFaces.contains(Direction.DOWN) && y < 0)) &&
                Math.abs(y - velocity.y) > 0.01 &&
                !jump && !step) {
            failedVelocity = true;
            return null;
        }

        double minX = x - maxDiscrepancy;
        double maxX = x + maxDiscrepancy;
        double minZ = z - maxDiscrepancy;
        double maxZ = z + maxDiscrepancy;
        if (!((touchFaces.contains(Direction.EAST) && x > 0) || (touchFaces.contains(Direction.WEST) && x < 0)) &&
                !(velocity.x <= maxX && velocity.x >= minX)) {
            failedVelocity = true;
            return null;
        }
        if (!((touchFaces.contains(Direction.SOUTH) && z > 0) || (touchFaces.contains(Direction.NORTH) && z < 0)) &&
                !(velocity.z <= maxZ && velocity.z >= minZ)) {
            failedVelocity = true;
            return null;
        }

        p.velocity.x = p.velocity.y = p.velocity.z = 0;

        return new Vector3D(x, y, z);
    }

    @Override
    public void post() {
        HPlayer.Physics physics = p.physics;

        physics.position = to;
        physics.wasOnGround = physics.onGround;
        physics.onGround = onGround;
        physics.onGroundReally = onGroundReally;
        physics.prevVelocity = physics.velocity;
        physics.velocity = velocity;
        physics.friction = newFriction;

        p.status.hitSlowdown = false;
        p.velocity.firstTick = false;
    }
}