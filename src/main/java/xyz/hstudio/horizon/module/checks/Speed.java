package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.SpeedData;
import xyz.hstudio.horizon.file.node.SpeedNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.Set;

public class Speed extends Module<SpeedData, SpeedNode> {

    public Speed() {
        super(ModuleType.Speed, new SpeedNode());
    }

    @Override
    public SpeedData getData(final HoriPlayer player) {
        return player.speedData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        event.setCancelled(true);
        Sync.teleport(player, player.position);
        if (type.equals("TypeA") && (player.isEating || player.isPullingBow)) {
            if (config.typeA_cancel_type == 1) {
                int slot = player.heldSlot + 1 > 8 ? 0 : player.heldSlot + 1;
                player.player.getInventory().setHeldItemSlot(slot);
            } else {
                McAccessor.INSTANCE.releaseItem(player.player);
                player.player.updateInventory();
            }
        }
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
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
     * Accuracy: 9/10 - It has some rare false positives.
     * Efficiency: 9/10 - Detects most move related hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

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

            if (e.knockBack != null || e.piston.size() > 0 || player.isFlying() || e.touchingFaces.contains(BlockFace.UP) ||
                    player.touchingFaces.contains(BlockFace.UP) || player.invalidMotionData.magic ||
                    player.invalidMotionData.prevGliding) {
                data.prevSpeed = speed;
                return;
            }
            if (e.isTeleport || player.getVehicle() != null ||
                    player.currentTick - player.leaveVehicleTick <= 1 || player.isGliding) {
                return;
            }

            boolean swimming = AABB.waterCollisionBox
                    .shrink(0.001, 0.001, 0.001)
                    .add(e.from.toVector())
                    .getMaterials(e.to.world)
                    .stream()
                    .anyMatch(MatUtils::isLiquid);
            boolean usingItem = player.isEating || player.isPullingBow || player.isBlocking;

            double estimatedSpeed;
            double discrepancy;

            if (swimming) {
                // Water function
                Vector3D move = e.velocity.clone().setY(0);
                Vector3D waterForce = e.waterFlowForce.clone().setY(0).normalize().multiply(0.014);
                double waterForceLength = waterForce.length();
                double computedForce = McAccessor.INSTANCE.cos(move.angle(waterForce)) * waterForceLength + 0.003;
                if (Double.isNaN(computedForce)) {
                    computedForce = waterForceLength;
                    if (Double.isNaN(computedForce)) {
                        computedForce = 0;
                    }
                }

                estimatedSpeed = this.waterMapping(prevSpeed, computedForce,
                        player.getEnchantmentEffectAmplifier("DEPTH_STRIDER"), player.isSprinting, e.onGround);
            } else {
                // Normal function
                boolean jump = e.jumpLegitly || (e.stepLegitly && player.isOnGround && player.isSprinting);

                float friction = e.oldFriction;
                float maxForce = this.computeMaxInputForce(player, e.newFriction, usingItem);

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
                if (touchedBlocks.contains(Material.SLIME_BLOCK) && Math.abs(player.velocity.y) < 0.1 && !player.isSneaking) {
                    multipliers *= 0.4 + Math.abs(player.velocity.y) * 0.2;
                }

                double adders = 0;
                if (player.isSprinting && jump) {
                    adders += 0.2;
                }

                estimatedSpeed = friction * prevSpeed * multipliers + (maxForce + adders + 0.000001);
            }
            discrepancy = speed - estimatedSpeed;
            if (e.updatePos) {
                if (discrepancy < 0 || speed > data.negativeDiscrepanciesCumulative) {
                    data.discrepancies = Math.max(data.discrepancies + discrepancy, 0);
                }
                double totalDiscrepancy = data.discrepancies;
                if (discrepancy > 0 && totalDiscrepancy > config.typeA_tolerance) {
                    if (!(!e.onGround && Math.abs(player.velocity.y - 0.33319999) < 0.001 &&
                            player.touchingFaces.size() > 0 && e.touchingFaces.size() == 0)) {
                        // Punish
                        this.punish(event, player, data, "TypeA", (float) (totalDiscrepancy * 10),
                                "s:" + speed, "e:" + estimatedSpeed, "p:" + prevSpeed, "d:" + discrepancy);
                    }
                    data.discrepancies = 0;
                } else {
                    reward("TypeA", data, 0.99);
                }
                data.negativeDiscrepancies = 0;
                data.negativeDiscrepanciesCumulative = 0;
            } else {
                data.negativeDiscrepancies = discrepancy;
                data.negativeDiscrepanciesCumulative = data.negativeDiscrepanciesCumulative + speed;
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
    private float computeMaxInputForce(final HoriPlayer player, final float newFriction, final boolean usingItem) {
        float initForce = 0.98F;
        if (player.isSneaking) {
            initForce *= 0.3;
        }
        if (usingItem) {
            initForce *= 0.2;
        }
        boolean sprinting = player.isSprinting && !player.isSneaking && player.getPotionEffectAmplifier("BLINDNESS") <= 0;

        float multiplier;
        if (player.isOnGround) {
            multiplier = player.moveFactor * 0.16277136F / (newFriction * newFriction * newFriction);
            float groundMultiplier = 5 * player.player.getWalkSpeed();
            multiplier *= groundMultiplier;

            float diagonal = (float) Math.sqrt(2 * initForce * initForce);
            if (diagonal < 1) {
                diagonal = 1F;
            }
            float componentForce = initForce * multiplier / diagonal;
            return (float) Math.sqrt(2 * componentForce * componentForce);
        } else {
            float flyMultiplier = 10 * player.player.getFlySpeed();
            multiplier = 0.02F * flyMultiplier;

            float diagonal = (float) Math.sqrt(2 * initForce * initForce);
            if (diagonal < 1) {
                diagonal = 1F;
            }
            float componentForce = initForce * multiplier / diagonal;
            float finalForce = (float) Math.sqrt(2 * componentForce * componentForce);
            return finalForce * (sprinting ? 1.3F : 1);
        }
    }

    private void typeB(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            boolean collisionHorizontal = e.touchingFaces.contains(BlockFace.NORTH) || e.touchingFaces.contains(BlockFace.SOUTH) ||
                    e.touchingFaces.contains(BlockFace.WEST) || e.touchingFaces.contains(BlockFace.EAST);

            if (!player.isSprinting) {
                data.lastSprintTick = player.currentTick;
            }

            // TODO: Ignore if colliding entities
            if (e.isInLiquid || e.isTeleport || e.knockBack != null || e.collidingBlocks.contains(Material.LADDER) ||
                    e.collidingBlocks.contains(Material.VINE) || (collisionHorizontal && !data.collisionHorizontal) ||
                    player.isFlying() || player.currentTick - data.lastSprintTick < 2 || player.getVehicle() != null ||
                    player.currentTick - player.leaveVehicleTick < 1 || e.velocity.clone().setY(0).lengthSquared() < 0.04 ||
                    e.piston.size() > 0 || e.isCollidingEntities) {
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
                // Punish
                this.punish(event, player, data, "TypeB", 4, "a:" + angle, "y:" + yaw);
            } else {
                reward("TypeB", data, 0.99);
            }

            data.collisionHorizontal = collisionHorizontal;
        }
    }
}