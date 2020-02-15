package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.config.checks.SpeedConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.SpeedData;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.Set;

public class Speed extends Module<SpeedData, SpeedConfig> {

    public Speed() {
        super(ModuleType.Speed, new SpeedConfig());
    }

    @Override
    public SpeedData getData(final HoriPlayer player) {
        return player.speedData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final SpeedData data, final SpeedConfig config) {
        event.setCancelled(true);
        Sync.teleport(player, player.position);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final SpeedData data, final SpeedConfig config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
        if (config.typeB_enabled) {
            typeB(event, player, data, config);
        }
    }

    /**
     * Basic x/z-axis motion check. Based on Minecraft moving mechanism.
     * <p>
     * Accuracy: 9/10 - It may have some rare false positives.
     * Efficiency: 9/10 - Detects most move related hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final SpeedData data, final SpeedConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            // TODO: Handle Speed Attribute

            double prevSpeed;
            double speed;
            if (e.updatePos) {
                prevSpeed = data.prevSpeed;
                if (data.noMoves > 0) {
                    prevSpeed = Math.min(prevSpeed, 0.03);
                }
                speed = MathUtils.distance2d(e.to.x - e.from.x, e.to.z - e.from.z);
            } else {
                speed = data.prevSpeed - data.negativeDiscrepancies + 0.000001;
                prevSpeed = speed;
            }

            if (e.updatePos) {
                data.noMoves = 0;
            } else {
                data.noMoves++;
            }

            if (e.knockBack != null) {
                data.prevSpeed = speed;
                return;
            }
            if (e.isTeleport) {
                return;
            }

            boolean flying = player.isFlying();
            boolean swimming = player.isInLiquid;

            double estimatedSpeed;
            double discrepancy;

            if (swimming && !flying) {
                // Water function
                Vector3D move = e.velocity.clone().setY(0);
                Vector3D waterForce = e.waterFlowForce.clone().setY(0).normalize().multiply(0.014);
                double waterForceLength = waterForce.length();
                double computedForce = McAccessor.INSTANCE.cos(move.angle(waterForce)) * waterForceLength + 0.003;

                estimatedSpeed = this.waterMapping(prevSpeed, computedForce,
                        player.getEnchantmentEffectAmplifier("DEPTH_STRIDER"), player.isSprinting, e.onGround);
            } else {
                // Normal function
                boolean jump = e.jumpLegitly || (e.stepLegitly && player.isOnGround && player.isSprinting);

                float friction = e.oldFriction;
                float maxForce = this.computeMaxInputForce(player, e.newFriction);

                Set<Material> touchedBlocks = e.cube.add(-e.velocity.x, -e.velocity.y, -e.velocity.z).getMaterials(e.to.world);

                double multipliers = 1;
                if (e.hitSlowdown) {
                    multipliers *= 0.6;
                }
                if (touchedBlocks.contains(Material.SOUL_SAND)) {
                    multipliers *= 0.4;
                }
                if (touchedBlocks.contains(MatUtils.COBWEB.parse())) {
                    multipliers *= 0.25;
                }
                if (touchedBlocks.contains(Material.SLIME_BLOCK) && MathUtils.abs(player.velocity.y) < 0.1 && !player.isSneaking) {
                    multipliers *= 0.4 + MathUtils.abs(player.velocity.y) * 0.2;
                }

                double adders = 0;
                if (player.isSprinting && jump) {
                    adders += 0.2;
                }

                estimatedSpeed = friction * prevSpeed * multipliers + (maxForce + adders + 0.000001);
            }
            discrepancy = speed - estimatedSpeed;
            if (e.updatePos) {
                if (discrepancy < 0 || speed > 0) {
                    data.discrepancies = Math.max(data.discrepancies + discrepancy, 0);
                }
                double totalDiscrepancy = data.discrepancies;
                if (discrepancy > 0 && totalDiscrepancy > config.typeA_tolerance) {
                    this.debug("Failed: TypeA, s:" + speed + ", e:" + estimatedSpeed + ", p:" + prevSpeed + ", d:" + discrepancy);

                    // Punish
                    this.punish(event, player, data, "TypeA", (float) (totalDiscrepancy * 10));
                    data.discrepancies = 0;
                } else {
                    reward("TypeA", data, 0.99);
                }
                data.negativeDiscrepancies = 0;
            } else {
                data.negativeDiscrepancies = discrepancy;
            }
            data.prevSpeed = speed;
        }
    }

    private double waterMapping(final double lastSpeed, final double waterFlowForce, final int level, final boolean sprinting, final boolean onGround) {
        float waterSlowDown = sprinting ? 0.9F : 0.8F;
        float depthStriderLevel = level;
        float acceleration = 0.02F;
        if (depthStriderLevel > 3) {
            depthStriderLevel = 3;
        }
        if (!onGround) {
            depthStriderLevel *= 0.5F;
        }
        if (depthStriderLevel > 0) {
            acceleration += (0.54600006F - acceleration) * depthStriderLevel / 3.0F;
        }
        return waterSlowDown * lastSpeed + acceleration + waterFlowForce;
    }

    /**
     * Get the max input force.
     *
     * @author Islandscout, MrCraftGoo
     */
    private float computeMaxInputForce(final HoriPlayer player, final float newFriction) {
        float initForce = 0.98F;
        if (player.isSneaking) {
            initForce *= 0.3;
        }
        boolean usingItem = player.isEating || player.isPullingBow;
        if (usingItem) {
            initForce *= 0.2;
        }
        boolean sprinting = player.isSprinting && !player.isSneaking && player.getPotionEffectAmplifier("BLINDNESS") <= 0;
        boolean flying = player.isFlying();

        float multiplier;
        if (player.isOnGround) {
            // float movementFactor = (float) McAccessor.INSTANCE.getMoveFactor(player.player);
            multiplier = 0.1F * 0.16277136F / (newFriction * newFriction * newFriction);
            float groundMultiplier = 5 * player.player.getWalkSpeed() * (1 + player.getPotionEffectAmplifier("SPEED") * 0.2F);
            multiplier *= groundMultiplier;
        } else {
            float flyMultiplier = 10 * player.player.getFlySpeed();
            multiplier = (flying ? 0.05F : 0.02F) * flyMultiplier;
        }

        float diagonal = (float) Math.sqrt(2 * initForce * initForce);
        if (diagonal < 1.0F) {
            diagonal = 1.0F;
        }
        float componentForce = initForce * multiplier / diagonal;
        float finalForce = (float) Math.sqrt(2 * componentForce * componentForce);
        return finalForce * (sprinting ? (flying ? 2 : 1.3F) : 1);
    }

    private void typeB(final Event event, final HoriPlayer player, final SpeedData data, final SpeedConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            boolean collisionHorizontal = e.touchingFaces.contains(BlockFace.NORTH) || e.touchingFaces.contains(BlockFace.SOUTH) ||
                    e.touchingFaces.contains(BlockFace.WEST) || e.touchingFaces.contains(BlockFace.EAST);

            if (!player.isSprinting) {
                data.lastSprintTick = player.currentTick;
            }

            // TODO: Ignore if colliding entities
            if (e.isInLiquid || e.isTeleport || e.knockBack != null ||
                    (collisionHorizontal && !data.collisionHorizontal) || player.isFlying() ||
                    player.currentTick - data.lastSprintTick < 2 || player.getVehicle() != null ||
                    e.velocity.clone().setY(0).lengthSquared() < 0.04) {
                return;
            }

            float yaw = e.to.yaw;
            Vector3D prevVelocity = player.velocity.clone();
            if (e.hitSlowdown) {
                prevVelocity.multiply(0.6);
            }
            double dX = e.to.x - e.from.x;
            double dZ = e.to.z - e.from.z;
            float friction = e.oldFriction;
            dX /= friction;
            dZ /= friction;
            if (e.jumpLegitly) {
                float yawRadians = yaw * 0.017453292F;
                dX += (McAccessor.INSTANCE.sin(yawRadians) * 0.2F);
                dZ -= (McAccessor.INSTANCE.cos(yawRadians) * 0.2F);
            }

            dX -= prevVelocity.x;
            dZ -= prevVelocity.z;

            Vector3D moveForce = new Vector3D(dX, 0, dZ);
            Vector3D yawVec = MathUtils.getDirection(yaw, 0);

            double angle = MathUtils.angle(yawVec, moveForce);
            if (angle > Math.PI / 4 + 0.3) {
                this.debug("Failed: TypeB, a:" + angle + ", y:" + yaw);

                // Punish
                this.punish(event, player, data, "TypeB", 4);
            } else {
                reward("TypeB", data, 0.99);
            }

            data.collisionHorizontal = collisionHorizontal;
        }
    }
}