package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.compat.McAccessor;
import xyz.hstudio.horizon.bukkit.config.checks.InvalidMotionConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.InvalidMotionData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.MatUtils;
import xyz.hstudio.horizon.bukkit.util.MathUtils;

public class InvalidMotion extends Module<InvalidMotionData, InvalidMotionConfig> {

    public InvalidMotion() {
        super(ModuleType.InvalidMotion, new InvalidMotionConfig());
    }

    @Override
    public InvalidMotionData getData(final HoriPlayer player) {
        return player.invalidMotionData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        typeA(event, player, data, config);
        typeB(event, player, data, config);
        typeC(event, player, data, config);
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

            float deltaY = (float) e.velocity.y;

            // TODO: Fix Cobweb, Slime handler
            // TODO: Fix the false positives when joining
            // TODO: Handle Water, Velocity, Vehicle

            if (!e.onGround && !e.jumpLegitly && !e.stepLegitly && !e.isTeleport && player.getVehicle() == null &&
                    !player.isFlying() && !e.isOnSlime && !e.isOnBed) {
                float prevEstimatedVelocity = data.estimatedVelocity;
                float estimatedVelocity;

                int levitation = player.getPotionEffectAmplifier("LEVITATION");
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
                    estimatedVelocity = (prevEstimatedVelocity - 0.08F) * 0.98F;
                }
                if (Math.abs(estimatedVelocity) < 0.005 && estimatedVelocity != 0) {
                    estimatedVelocity = deltaY;
                }
                boolean hitHead = e.touchingFaces.contains(BlockFace.UP);
                boolean hasHitHead = player.touchingFaces.contains(BlockFace.UP);
                if (hitHead && !hasHitHead) {
                    deltaY = estimatedVelocity = 0;
                }

                // Fix the false positives when towering
                if (!player.isOnGround && player.prevDeltaY == 0 && (MathUtils.abs(deltaY - 0.40444491418477) < 0.001 || MathUtils.abs(deltaY - 0.39557589867329) < 0.001)) {
                    deltaY = estimatedVelocity = 0.42F;
                }

                // I don't know why but client considers player is not on ground when walking on slime,
                // client bug?
                Block standing = e.to.add(0, -0.01, 0).getBlock();
                if (e.onGroundReally && deltaY == 0 && standing != null && standing.getType() == MatUtils.SLIME_BLOCK.parse()) {
                    estimatedVelocity = 0;
                }

                float discrepancy = deltaY - estimatedVelocity;

                if (e.updatePos && e.velocity.lengthSquared() > 0 && MathUtils.abs(discrepancy) > 0.001) {
                    this.debug("Failed: TypeA, d:" + deltaY + ", e:" + estimatedVelocity + ", p:" + discrepancy);

                    // Punish
                    this.punish(player, data, "TypeA", MathUtils.abs(discrepancy) * 5);
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
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            // TODO: Handle Velocity
            if (e.stepLegitly || !e.onGround || !player.isOnGround || e.isTeleport) {
                return;
            }
            double deltaY = e.velocity.y;

            if (deltaY > 0.6 || deltaY < -0.0784) {
                this.debug("Failed: TypeB, d:" + deltaY);

                // Punish
                this.punish(player, data, "TypeB", 4);
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
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;

            // TODO: Ignore Velocity and Liquid
            if (e.isTeleport || e.touchingFaces.contains(BlockFace.UP) || e.collidingBlocks.contains(MatUtils.LADDER.parse()) ||
                    e.collidingBlocks.contains(MatUtils.VINE.parse()) || player.isFlying() || player.player.isSleeping() ||
                    player.position.isOnGround(false, 0.001)) {
                return;
            }
            double deltaY = e.velocity.y;
            double estimatedY = (player.velocity.y - 0.08) * 0.98;

            double discrepancy = deltaY - estimatedY;
            if (discrepancy < -0.02) {
                this.debug("Failed: TypeC, d:" + deltaY);

                // Punish
                this.punish(player, data, "TypeC", 4);
            } else {
                reward("TypeC", data, 0.99);
            }
        }
    }
}