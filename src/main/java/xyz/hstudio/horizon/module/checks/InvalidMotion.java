package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.config.checks.InvalidMotionConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.InvalidMotionData;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.enums.MatUtils;

import java.util.Objects;

public class InvalidMotion extends Module<InvalidMotionData, InvalidMotionConfig> {

    public InvalidMotion() {
        super(ModuleType.InvalidMotion, new InvalidMotionConfig());
    }

    @Override
    public InvalidMotionData getData(final HoriPlayer player) {
        return player.invalidMotionData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        event.setCancelled(true);
        Sync.teleport(player, player.position);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
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
    private void typeA(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            if (player.currentTick - player.lastTeleportAcceptTick < 3) {
                data.estimatedVelocity = 0;
                return;
            }

            float deltaY = (float) e.velocity.y;

            // TODO: Fix Cobweb, Slime handler
            // TODO: Handle Vehicle

            if (!e.onGround && !e.jumpLegitly && !e.stepLegitly && e.knockBack == null &&
                    player.getVehicle() == null && !player.isFlying() && !e.isOnSlime && !e.isOnBed &&
                    !e.isInLiquid && !player.isInLiquid && !e.collidingBlocks.contains(Material.LADDER) &&
                    !e.collidingBlocks.contains(Material.VINE)) {

                int levitation = player.getPotionEffectAmplifier("LEVITATION");
                // Supports SLOW_FALLING potion effect
                float gravity = player.getPotionEffectAmplifier("SLOW_FALLING") > 0 && player.velocity.y < 0 ? 0.01F : 0.08F;
                float prevEstimatedVelocity = data.estimatedVelocity;
                float estimatedVelocity;

                if (player.isGliding) {
                    // Gliding function
                    float pitchRot = (float) Math.toRadians(e.to.pitch);
                    float magic = McAccessor.INSTANCE.cos(pitchRot);
                    magic = (float) (magic * magic * Math.min(1, e.to.getDirection().length() / 0.4F));
                    estimatedVelocity = prevEstimatedVelocity - 0.08F + magic * 0.06F;
                    if (estimatedVelocity < 0) {
                        estimatedVelocity += estimatedVelocity * -0.1F * magic;
                    }
                    if (pitchRot < 0) {
                        estimatedVelocity += player.velocity.clone().setY(0).length() *
                                (-McAccessor.INSTANCE.sin(pitchRot)) * 0.04F * 3.2F;
                    }
                    estimatedVelocity *= 0.98F;
                } else if (levitation > 0) {
                    estimatedVelocity = prevEstimatedVelocity + (0.05F * levitation - prevEstimatedVelocity) * 0.2F;
                } else if (e.collidingBlocks.contains(MatUtils.COBWEB.parse())) {
                    estimatedVelocity = -0.00392F;
                } else {
                    estimatedVelocity = (prevEstimatedVelocity - gravity) * 0.98F;
                }
                if (MathUtils.abs(estimatedVelocity) < 0.005 && estimatedVelocity != 0) {
                    estimatedVelocity = deltaY;
                }

                // Fix the false positives when there're blocks above
                boolean hitHead = e.touchingFaces.contains(BlockFace.UP);
                boolean hasHitHead = player.touchingFaces.contains(BlockFace.UP);
                if (hitHead && !hasHitHead) {
                    deltaY = estimatedVelocity = 0;
                }

                // Fix the false positives when towering
                if (!player.isOnGround && player.velocity.y == 0 && (MathUtils.abs(deltaY - 0.40444491418477) < 0.001 || MathUtils.abs(deltaY - 0.39557589867329) < 0.001)) {
                    deltaY = estimatedVelocity = 0.42F;
                }

                // Idk why but client considers player is not on ground when walking on slime, client bug?
                if (e.onGroundReally && !e.onGround && deltaY == 0 && BlockUtils.getBlocksInLocation(e.to.add(0, -0.1, 0))
                        .stream()
                        .filter(Objects::nonNull)
                        .anyMatch(b -> b.getType() == MatUtils.SLIME_BLOCK.parse())) {
                    estimatedVelocity = 0;
                }

                float discrepancy = deltaY - estimatedVelocity;

                if (e.updatePos && e.velocity.lengthSquared() > 0 && MathUtils.abs(discrepancy) > config.typeA_tolerance) {
                    this.debug("Failed: TypeA, d:" + deltaY + ", e:" + estimatedVelocity + ", p:" + discrepancy);

                    // Punish
                    this.punish(event, player, data, "TypeA", MathUtils.abs(discrepancy) * 5F);
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
    private void typeB(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.stepLegitly || !e.onGround || !player.isOnGround || e.isTeleport || e.knockBack != null) {
                return;
            }
            double deltaY = e.velocity.y;

            if (deltaY > 0.6 || deltaY < -0.0784) {
                this.debug("Failed: TypeB, d:" + deltaY);

                // Punish
                this.punish(event, player, data, "TypeB", 4);
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
    private void typeC(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            // TODO: Ignore Liquid
            if (e.isTeleport || !e.onGround || e.knockBack != null || e.touchingFaces.contains(BlockFace.UP) || player.touchingFaces.contains(BlockFace.UP) ||
                    e.collidingBlocks.contains(MatUtils.LADDER.parse()) || e.collidingBlocks.contains(MatUtils.VINE.parse()) ||
                    player.isFlying() || player.player.isSleeping() || player.position.isOnGround(player, false, 0.001)) {
                return;
            }
            double deltaY = e.velocity.y;
            double estimatedY = (player.velocity.y - 0.08) * 0.98;

            double discrepancy = deltaY - estimatedY;
            if (discrepancy < config.typeC_tolerance) {
                this.debug("Failed: TypeC, d:" + deltaY);

                // Punish
                this.punish(event, player, data, "TypeC", 4);
            } else {
                reward("TypeC", data, 0.99);
            }
        }
    }
}