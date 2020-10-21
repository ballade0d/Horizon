package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.SpeedData;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.ActionEvent;
import xyz.hstudio.horizon.event.inbound.InteractItemEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.file.node.SpeedNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.Set;

public class Speed extends Module<SpeedData, SpeedNode> {

    public Speed() {
        // The first NoSlow is for Packet NoSlow
        // The second is for Move NoSlow
        super(ModuleType.Speed, new SpeedNode(), "Predict", "NoSlow", "NoSlow", "Sprint", "StrafeA", "StrafeB");
    }

    @Override
    public SpeedData getData(final HoriPlayer player) {
        return player.speedData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        if (type == 2 && (player.isEating || player.isPullingBow || player.isBlocking)) {
            McAccessor.INSTANCE.ensureMainThread(() -> {
                McAccessor.INSTANCE.releaseItem(player.getPlayer());
                player.getPlayer().updateInventory();
            });
        } else if (type == 1) {
            event.setCancelled(true);
            if (event instanceof MoveEvent) {
                Sync.teleport(player, player.position);
            }
        } else {
            if (type == 0 && event instanceof MoveEvent && ((MoveEvent) event).hitSlowdown) {
                return;
            }
            event.setCancelled(true);
            Sync.teleport(player, player.position);
        }
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        if (config.predict_enabled || config.noslow_enabled) {
            typeA(event, player, data, config, config.noslow_enabled);
        }
        if (config.sprint_enabled) {
            typeB(event, player, data, config);
        }
        if (config.strafe_enabled) {
            typeC(event, player, data, config);
            typeD(event, player, data, config);
        }
    }

    /**
     * Basic x/z-axis motion check. Based on Minecraft moving mechanism.
     * <p>
     * Accuracy: 9/10 - It has some rare false positives.
     * Efficiency: 9/10 - Detects most move related hacks instantly.
     *
     * @author MrCraftGoo, Cipher
     */
    private void typeA(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config, final boolean noSlowEnabled) {
        if (event instanceof MoveEvent) {
            // Avoid ping influence
            if (player.foodLevel < 6 && player.isSprinting) {
                this.punish(event, player, data, 1, 4);
            } else {
                this.reward(1, data, 0.99);
            }
        }
        if (event instanceof InteractItemEvent && noSlowEnabled) {
            if (player.protocol != 47) {
                return;
            }
            InteractItemEvent e = (InteractItemEvent) event;
            if (e.interactType == InteractItemEvent.InteractType.RELEASE_USE_ITEM ||
                    (e.interactType == InteractItemEvent.InteractType.START_USE_ITEM && e.useItem)) {
                if (player.currentTick == data.lastUseTick) {
                    this.punish(event, player, data, 1, config.noslow_packet_vl, config.noslow_always_cancel, "t:p1");
                }
                data.lastUseTick = player.currentTick;
            }
        } else if (event instanceof ActionEvent && noSlowEnabled) {
            if (player.protocol != 47) {
                return;
            }
            ActionEvent e = (ActionEvent) event;
            if (e.action != ActionEvent.Action.START_SNEAKING &&
                    e.action != ActionEvent.Action.STOP_SNEAKING) {
                return;
            }
            if (player.currentTick == data.lastToggleTick) {
                this.punish(event, player, data, 1, config.noslow_packet_vl, config.noslow_always_cancel, "t:p2");
            }
            data.lastToggleTick = player.currentTick;
        } else if (event instanceof MoveEvent) {
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

            if (e.knockBack != null || player.isFlying() || e.touchingFaces.contains(BlockFace.UP) ||
                    player.touchingFaces.contains(BlockFace.UP) || player.invalidMotionData.magic ||
                    player.isGliding || player.invalidMotionData.prevGliding || data.attributeBypass || e.piston) {
                data.prevSpeed = speed;
                return;
            }
            if (e.isTeleport || player.getVehicle() != null || player.vehicleBypass) {
                return;
            }

            boolean swimming = player.isInWater;
            boolean usingItem = player.isEating || player.isPullingBow || player.isBlocking;

            boolean jump = e.jumpLegitly || (e.stepLegitly && player.onGround && player.isSprinting);

            float friction = e.oldFriction;
            float maxForce = computeMaxInputForce(player, e.newFriction, usingItem, e.onGround);

            Set<Material> touchedBlocks = e.touchedBlocks;

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
            if (swimming && player.protocol <= 340) {
                // Swimming function
                Vector3D move = e.velocity.clone().setY(0);
                Vector3D waterForce = e.waterFlowForce.clone().setY(0);
                double waterForceLength = waterForce.length() + 0.003;
                double moveLength = move.length();
                double computedForce = moveLength == 0 ? waterForceLength : (move.dot(waterForce) / moveLength);

                adders += computedForce;
            }

            double estimatedSpeed = friction * prevSpeed * multipliers + (maxForce + adders + 0.000001);
            double discrepancy = speed - estimatedSpeed;
            if (e.updatePos) {
                if (discrepancy < 0 || speed > data.negativeDiscrepanciesCumulative) {
                    data.discrepancies = Math.max(data.discrepancies + discrepancy, 0);
                }
                double totalDiscrepancy = data.discrepancies;
                if (discrepancy > 0 && totalDiscrepancy > config.predict_tolerance) {
                    if (!(!e.onGround && Math.abs(player.velocity.y - 0.33319999) < 0.001 &&
                            player.touchingFaces.size() > 0 && e.touchingFaces.size() == 0)) {
                        if (usingItem) {
                            if (noSlowEnabled) {
                                if (data.flagNextTick) {
                                    // Punish
                                    this.punish(event, player, data, 2,
                                            config.noslow_move_vl == -1 ? (float) (totalDiscrepancy * 10F) : config.noslow_move_vl, config.noslow_always_cancel,
                                            "s:" + speed, "e:" + estimatedSpeed, "p:" + prevSpeed, "d:" + discrepancy, "s:" + swimming);
                                }
                                data.flagNextTick = true;
                            }
                        } else {
                            // Punish
                            this.punish(event, player, data, 0, (float) (totalDiscrepancy * 10),
                                    "s:" + speed, "e:" + estimatedSpeed, "p:" + prevSpeed, "d:" + discrepancy, "s:" + swimming);
                            if (e.hitSlowdown) {
                                player.killAuraData.failKeepSprintTick = player.currentTick;
                            }
                        }
                    }
                    data.discrepancies = 0;
                } else {
                    reward(0, data, 0.99);
                }
                data.negativeDiscrepancies = 0;
                data.negativeDiscrepanciesCumulative = 0;
            } else {
                data.negativeDiscrepancies = discrepancy;
                data.negativeDiscrepanciesCumulative = Math.min(data.negativeDiscrepanciesCumulative + speed, 0.03 + speed);
            }
            data.prevSpeed = e.collidingBlocks.contains(MatUtils.COBWEB.parse()) ? 0 : speed;
        }
    }

    /**
     * Get the max input force.
     *
     * @author Islandscout, MrCraftGoo
     */
    private float computeMaxInputForce(final HoriPlayer player, final float newFriction, final boolean usingItem, final boolean onGround) {
        float initForce = 0.98F;
        if (player.isSneaking) {
            initForce *= 0.3;
        }
        if (usingItem) {
            initForce *= 0.2;
        }
        boolean sprinting = player.isSprinting && !player.isSneaking && player.getPotionEffectAmplifier("BLINDNESS") <= 0;

        float multiplier;
        if (player.isInWater && !player.isFlying()) {
            float force = 0.02F;
            float depthStrider = player.getEnchantmentEffectAmplifier("DEPTH_STRIDER");
            if (depthStrider > 3) {
                depthStrider = 3;
            }
            if (!onGround) {
                depthStrider *= 0.5F;
            }
            if (depthStrider > 0) {
                force += (0.1F * 1.0F - force) * depthStrider / 3F;
            }
            return force;
        } else if (player.isInLava && !player.isFlying()) {
            return 0.02F;
        } else if (player.onGround) {
            multiplier = player.getMoveFactor() * 0.16277136F / (newFriction * newFriction * newFriction);
            float groundMultiplier = 5 * player.getPlayer().getWalkSpeed();
            multiplier *= groundMultiplier;

            float diagonal = (float) Math.sqrt(2 * initForce * initForce);
            if (diagonal < 1) {
                diagonal = 1F;
            }
            float componentForce = initForce * multiplier / diagonal;
            return (float) Math.sqrt(2 * componentForce * componentForce);
        } else {
            float flyMultiplier = 10 * player.getPlayer().getFlySpeed();
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

    /**
     * Sprint check.
     * <p>
     * Accuracy: 9/10 - It may have some rare false positives.
     * Efficiency: 10/10 - Detects Sprint hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            boolean collisionHorizontal = e.touchingFaces.contains(BlockFace.NORTH) || e.touchingFaces.contains(BlockFace.SOUTH) ||
                    e.touchingFaces.contains(BlockFace.WEST) || e.touchingFaces.contains(BlockFace.EAST);

            if (!player.isSprinting) {
                data.lastSprintTick = player.currentTick;
            }

            if (e.isInWater || player.currentTick - player.teleportAcceptTick < 3 || e.knockBack != null ||
                    e.collidingBlocks.contains(Material.LADDER) || e.collidingBlocks.contains(Material.VINE) ||
                    (collisionHorizontal && !data.collisionHorizontal) || player.isFlying() ||
                    player.currentTick - data.lastSprintTick < 2 || player.getVehicle() != null ||
                    player.vehicleBypass || e.velocity.clone().setY(0).lengthSquared() < 0.04 ||
                    e.piston || !e.collidingEntities.isEmpty() || data.collisionHorizontal) {
                return;
            }

            float yaw = e.to.yaw;
            Vector3D prevVelocity = player.velocity.clone();
            if (e.touchedBlocks.contains(Material.SOUL_SAND)) {
                prevVelocity.multiply(0.4);
            }
            if (e.touchedBlocks.contains(Material.WEB)) {
                prevVelocity.multiply(0);
            }
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
                if (++data.sprintFails > 2) {
                    // Punish
                    this.punish(event, player, data, 3, 4, "a:" + angle, "y:" + yaw);
                }
            } else if (data.sprintFails == 0) {
                reward(3, data, 0.99);
            } else {
                data.sprintFails--;
            }

            data.collisionHorizontal = collisionHorizontal;
        }
    }

    /**
     * Strafe check, mainly detects on-ground strafe
     * <p>
     * Accuracy: 9/10 - It may have some rare false positives.
     * Efficiency: 9/10 - Detects almost instantly.
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.isTeleport || e.knockBack != null || e.piston) {
                return;
            }
            if (e.strafeNormally) {
                data.typeCFails = data.typeCFails > 0 ? data.typeCFails - 1 : 0;
                reward(4, data, 0.995);
                return;
            }
            if (++data.typeCFails > config.strafe_threshold) {
                // Punish
                this.punish(event, player, data, 4, 2);
            }
        }
    }

    /**
     * Strafe check, mainly detects air strafe
     * <p>
     * Accuracy: 9/10 - It may have some rare false positives.
     * Efficiency: 10/10 - Detects instantly.
     *
     * @author Islandscout, MrCraftGoo
     */
    private void typeD(final Event event, final HoriPlayer player, final SpeedData data, final SpeedNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            if (player.isFlying() || e.onGround || player.onGround || e.isTeleport || e.knockBack != null ||
                    !e.touchingFaces.isEmpty() || e.piston || player.getVehicle() != null || e.isInLiquid ||
                    BlockUtils.blockNearbyIsSolid(e.to) || BlockUtils.blockNearbyIsSolid(e.to.add(0, 1, 0))) {
                return;
            }

            Vector3D move = e.velocity.clone().setY(0);
            Vector3D prevMove = player.velocity.clone().setY(0);
            double deltaAngle = MathUtils.angle(move, prevMove);

            double prevSpeed = e.hitSlowdown ? prevMove.length() * 0.6 : prevMove.length();
            double magnitude = e.oldFriction * prevSpeed - 0.026001;

            if (move.lengthSquared() > 0.05 && deltaAngle > 0.2) {
                if (++data.typeDFails > 1) {
                    // Punish
                    this.punish(event, player, data, 5, 1, "t:a");
                }
            } else if (prevMove.lengthSquared() > 0.01 && move.length() < magnitude) {
                if (++data.typeDFails > 1) {
                    // Punish
                    this.punish(event, player, data, 5, 1, "t:b");
                }
            } else if (data.typeDFails > 0) {
                data.typeDFails--;
            } else {
                reward(5, data, 0.999);
            }
        }
    }
}