package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.ActionEvent;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.InvalidMotionData;
import xyz.hstudio.horizon.file.node.InvalidMotionNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.Vector3D;

import java.util.Objects;

public class InvalidMotion extends Module<InvalidMotionData, InvalidMotionNode> {

    public InvalidMotion() {
        super(ModuleType.InvalidMotion, new InvalidMotionNode());
    }

    @Override
    public InvalidMotionData getData(final HoriPlayer player) {
        return player.invalidMotionData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        event.setCancelled(true);
        Sync.teleport(player, player.position);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        data.magic = false;
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
        if (config.typeB_enabled) {
            typeB(event, player, data, config);
        }
        if (config.typeC_enabled) {
            typeC(event, player, data, config);
        }
    }

    /**
     * Basic y-axis motion check. Based on Minecraft moving mechanism.
     * <p>
     * Accuracy: 7/10 - It may have some false positives.
     * Efficiency: 10/10 - Detects a lot of move related hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            float deltaY = (float) e.velocity.y;

            if (player.currentTick - player.lastTeleportAcceptTick < 3) {
                data.estimatedVelocity = deltaY;
                return;
            }

            // TODO: Fix Cobweb, Slime handler
            // TODO: Handle Vehicle

            if (!e.onGround && !e.jumpLegitly && !e.stepLegitly && e.knockBack == null && e.piston.size() == 0 &&
                    player.currentTick - player.leaveVehicleTick > 1 && player.getVehicle() == null && !player.isFlying() &&
                    !player.player.isDead() && !e.isOnSlime && !e.isOnBed && !e.isInLiquid && !player.isInLiquid &&
                    !e.collidingBlocks.contains(Material.LADDER) && !e.collidingBlocks.contains(Material.VINE) &&
                    !e.collidingBlocks.contains(MatUtils.SCAFFOLDING.parse()) && !e.collidingBlocks.contains(MatUtils.KELP.parse()) &&
                    !e.collidingBlocks.contains(MatUtils.KELP_PLANT.parse()) && !e.collidingBlocks.contains(MatUtils.BUBBLE_COLUMN.parse()) &&
                    e.collidingBlocks.stream().noneMatch(BlockUtils.SHULKER_BOX::contains)) {

                int levitation = player.getPotionEffectAmplifier("LEVITATION");
                // Supports SLOW_FALLING potion effect
                float gravity = player.getPotionEffectAmplifier("SLOW_FALLING") > 0 && player.velocity.y < 0 ? 0.01F : 0.08F;
                float prevEstimatedVelocity = data.estimatedVelocity;
                float estimatedVelocity;

                if (player.isGliding) {
                    // Gliding function
                    // Fix this, this creates possibility of bypass
                    if (data.prevGliding) {
                        Vector3D lookDir = e.to.getDirection();
                        float lookDist = (float) MathUtils.distance2d(lookDir.x, lookDir.z);
                        float pitchRot = (float) Math.toRadians(e.to.pitch);
                        float magic = McAccessor.INSTANCE.cos(pitchRot);
                        magic = (float) (magic * magic * Math.min(1, e.to.getDirection().length() / 0.4F));
                        estimatedVelocity = (prevEstimatedVelocity - 0.08F + magic * 0.06F);
                        if (estimatedVelocity < 0 && lookDist > 0) {
                            estimatedVelocity += estimatedVelocity * -0.1F * magic;
                        }
                        if (pitchRot < 0) {
                            estimatedVelocity += player.velocity.clone().setY(0).length() *
                                    (-McAccessor.INSTANCE.sin(pitchRot)) * 0.04F * 3.2F;
                        }
                        estimatedVelocity *= 0.98F;
                    } else {
                        estimatedVelocity = deltaY;
                    }
                } else if (data.prevGliding) {
                    estimatedVelocity = 0;
                } else if (player.currentTick - data.attemptGlideTick < 2) {
                    estimatedVelocity = deltaY;
                } else if (levitation > 0) {
                    estimatedVelocity = prevEstimatedVelocity + (0.05F * levitation - prevEstimatedVelocity) * 0.2F;
                } else if (e.collidingBlocks.contains(MatUtils.COBWEB.parse())) {
                    estimatedVelocity = -0.00392F;
                } else {
                    estimatedVelocity = (prevEstimatedVelocity - gravity) * 0.98F;
                }
                if (Math.abs(estimatedVelocity) < 0.005 && estimatedVelocity != 0) {
                    estimatedVelocity = deltaY;
                }

                // Fix the false positives when there're blocks above
                boolean hitHead = e.touchingFaces.contains(BlockFace.UP);
                boolean hasHitHead = player.touchingFaces.contains(BlockFace.UP);
                if (hitHead && !hasHitHead) {
                    deltaY = estimatedVelocity = 0;
                }
                if (player.velocity.y == 0F && (estimatedVelocity == -0.0784F || estimatedVelocity == 0F) && player.isOnGround && deltaY >= 0 && deltaY < 0.419F) {
                    estimatedVelocity = deltaY;
                    data.magic = true;
                }

                // Fix the false positives when towering
                if (player.isOnGround && !e.onGround && player.velocity.y == 0 && (Math.abs(deltaY - 0.4044449) < 0.001 || Math.abs(deltaY - 0.3955759) < 0.001)) {
                    deltaY = estimatedVelocity = 0.42F;
                }

                // Idk why but client considers player is not on ground when walking on slime, client bug?
                if (e.onGroundReally && !e.onGround && deltaY == 0 && BlockUtils
                        .getBlocksInLocation(e.to.add(0, -0.2, 0))
                        .stream()
                        .filter(Objects::nonNull)
                        .anyMatch(b -> b.getType() == MatUtils.SLIME_BLOCK.parse())) {
                    estimatedVelocity = 0;
                }

                // Fix a weird bug in 1.9+
                if (deltaY < 0 && player.prevPrevDeltaY >= 0 && MathUtils.distance2d(e.to.x - e.from.x, e.to.z - e.from.z) < 0.1) {
                    estimatedVelocity = deltaY;
                }

                float discrepancy = deltaY - estimatedVelocity;

                if (e.updatePos && e.velocity.lengthSquared() > 0 && Math.abs(discrepancy) > config.typeA_tolerance) {
                    // Punish
                    this.punish(event, player, data, "TypeA", Math.abs(discrepancy) * 5F,
                            "d:" + deltaY, "e:" + estimatedVelocity, "p:" + discrepancy, "pr:" + player.velocity.y);
                } else {
                    reward("TypeA", data, 0.999);
                }

                data.estimatedVelocity = estimatedVelocity;
            } else {
                if (e.onGround || (e.touchingFaces.contains(BlockFace.UP) && deltaY > 0)) {
                    data.estimatedVelocity = 0;
                } else {
                    data.estimatedVelocity = deltaY;
                }
            }
            data.prevGliding = player.isGliding;
        } else if (event instanceof ActionEvent) {
            ActionEvent e = (ActionEvent) event;
            if (e.action == ActionEvent.Action.START_GLIDING) {
                data.attemptGlideTick = player.currentTick;
            }
        }
    }

    /**
     * A simple Step check
     * <p>
     * Accuracy: 10/10 - Should not have any false positives.
     * Efficiency: 8/10 - Detects a lot of types of step instantly.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.stepLegitly || !e.onGround || !player.isOnGround || e.isTeleport || e.knockBack != null) {
                return;
            }
            double deltaY = e.velocity.y;

            if (deltaY > 0.6 || deltaY < -0.0784) {
                // Punish
                this.punish(event, player, data, "TypeB", 4, "d:" + deltaY);
            } else {
                reward("TypeB", data, 0.99);
            }
        }
    }

    /**
     * A simple FastFall check.
     * TypeA also checks for FastFall, but it only detects if onGround == false,
     * however, if the client falls too fast, onGround will be true,
     * this check will fix the bypass.
     * <p>
     * Accuracy: 9/10 - Should not have any false positives.
     * Efficiency: 10/10 - Detects most FastFall instantly.
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            if (e.isTeleport || !e.onGround || e.knockBack != null || e.touchingFaces.contains(BlockFace.UP) || player.touchingFaces.contains(BlockFace.UP) ||
                    e.collidingBlocks.contains(MatUtils.LADDER.parse()) || e.collidingBlocks.contains(MatUtils.VINE.parse()) ||
                    player.isFlying() || player.player.isSleeping() || player.position.isOnGround(player, false, 0.001)) {
                return;
            }
            double deltaY = e.velocity.y;
            double estimatedY = (player.velocity.y - 0.08) * 0.98;

            double discrepancy = deltaY - estimatedY;
            if (discrepancy < config.typeC_tolerance) {
                // Punish
                this.punish(event, player, data, "TypeC", 4, "d:" + deltaY);
            } else {
                reward("TypeC", data, 0.99);
            }
        }
    }

    // Liquid function
    private void typeD(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {

    }

    // Ladder function
    private void typeE(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {

    }
}