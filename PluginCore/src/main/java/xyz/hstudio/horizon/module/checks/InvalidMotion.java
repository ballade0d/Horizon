package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.InvalidMotionData;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.AbilitiesEvent;
import xyz.hstudio.horizon.event.inbound.ActionEvent;
import xyz.hstudio.horizon.event.inbound.MoveEvent;
import xyz.hstudio.horizon.file.node.InvalidMotionNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;
import xyz.hstudio.horizon.util.BlockUtils;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.enums.MatUtils;
import xyz.hstudio.horizon.util.wrap.Vector3D;
import xyz.hstudio.horizon.wrap.IWrappedBlock;

import java.util.Objects;
import java.util.Set;

public class InvalidMotion extends Module<InvalidMotionData, InvalidMotionNode> {

    public InvalidMotion() {
        super(ModuleType.InvalidMotion, new InvalidMotionNode(), "Predict", "Step", "Packet");
    }

    @Override
    public InvalidMotionData getData(final HoriPlayer player) {
        return player.invalidMotionData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        event.setCancelled(true);
        Sync.teleport(player, type == 1 && data.safeLoc != null ? data.safeLoc : player.position);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        data.magic = false;
        if (config.predict_enabled) {
            typeA(event, player, data, config);
        }
        if (config.step_enabled) {
            typeB(event, player, data, config);
        }
        if (config.packet_enabled) {
            typeC(event, player, data, config);
        }
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.clientBlock != -1 && player.clientBlockCount > config.allowed_client_blocks) {
                cancel(event, 1, player, data, config);
            }
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

            // TODO: Handle Vehicle

            if (!player.isFlying() && (!e.onGround || !player.onGround) && !e.isTeleport &&
                    !e.jumpLegitly && !e.stepLegitly && e.knockBack == null && !e.piston &&
                    !e.isInLiquid && !player.vehicleBypass && player.getVehicle() == null &&
                    !player.getPlayer().isDead() && !e.isOnBed && !inSpecialBlock(e.collidingBlocks)) {

                int levitation = player.getPotionEffectAmplifier("LEVITATION");
                // Supports SLOW_FALLING potion effect
                float gravity = player.getPotionEffectAmplifier("SLOW_FALLING") > 0 && player.velocity.y < 0 ? 0.01F : 0.08F;
                float prevEstimatedVelocity = data.estimatedVelocity;
                float estimatedVelocity;

                IWrappedBlock feetBlock = e.to.add(0, -1, 0).getBlock();

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
                    // Glide landing
                    estimatedVelocity = deltaY;
                } else if (player.currentTick - data.attemptGlideTick < 2) {
                    // Fix glide false positive
                    estimatedVelocity = deltaY;
                } else if (levitation > 0) {
                    // Handle levitation potion effect
                    estimatedVelocity = prevEstimatedVelocity + (0.05F * levitation - prevEstimatedVelocity) * 0.2F;
                } else if (e.touchedBlocks.contains(MatUtils.COBWEB.parse())) {
                    // Handle Cobweb
                    // TODO: Fix this
                    estimatedVelocity = (prevEstimatedVelocity - 0.08F) * 0.98F * 0.05F;
                } else if (inLadder(e.collidingBlocks) || (feetBlock != null &&
                        (feetBlock.getType() == Material.LADDER ||
                                feetBlock.getType() == Material.VINE))) {
                    // TODO: Handle it
                    estimatedVelocity = deltaY;
                } else {
                    // Normal Air function
                    estimatedVelocity = (prevEstimatedVelocity - gravity) * 0.98F;
                }
                if (Math.abs(estimatedVelocity) < 0.005 && estimatedVelocity != 0 && deltaY <= 0) {
                    estimatedVelocity = deltaY;
                }

                // Fix the false positives when there're blocks above
                boolean hitHead = e.touchingFaces.contains(BlockFace.UP);
                boolean hasHitHead = player.touchingFaces.contains(BlockFace.UP);
                if (hitHead && !hasHitHead) {
                    deltaY = estimatedVelocity = 0;
                }
                if (Math.abs(deltaY + 0.0784) < 0.001 && !e.onGround &&
                        (Math.abs(player.velocity.y - 0.2) < 0.001 || Math.abs(player.velocity.y - 0.325) < 0.001 || Math.abs(player.velocity.y - 0.0125) < 0.001)) {
                    estimatedVelocity = deltaY;
                }
                // Fix the false positives when there's trapdoor above and there's slime under
                if (deltaY == 0 && estimatedVelocity == -0.0784F && (Math.abs(player.velocity.y - 0.0125) < 0.001 || player.velocity.y == 0F) && !e.onGround && !player.onGround && feetBlock != null && feetBlock.getType() == Material.SLIME_BLOCK) {
                    estimatedVelocity = deltaY;
                }

                // Fix the false positives when towering
                if (player.onGround && !e.onGround && player.velocity.y == 0 && (Math.abs(deltaY - 0.4044449) < 0.001 || Math.abs(deltaY - 0.3955759) < 0.001)) {
                    deltaY = estimatedVelocity = 0.42F;
                }

                // Idk why but client considers player is not on ground when walking on slime, client bug?
                if (Math.abs(deltaY) < 0.1 && e.onGroundReally && BlockUtils
                        .getBlocksInLocation(e.to.add(0, -0.2, 0))
                        .stream()
                        .filter(Objects::nonNull)
                        .anyMatch(b -> b.getType() == Material.SLIME_BLOCK)) {
                    estimatedVelocity = 0;
                }

                // Fix a weird bug in 1.9+
                if (deltaY < 0 && player.prevPrevDeltaY >= 0 && MathUtils.distance2d(e.to.x - e.from.x, e.to.z - e.from.z) < 0.001) {
                    estimatedVelocity = deltaY;
                }

                if (player.currentTick - player.teleportAcceptTick < 2) {
                    estimatedVelocity = 0;
                }

                float discrepancy = deltaY - estimatedVelocity;
                if (e.onGround && !player.onGround) {
                    if (Math.abs(discrepancy) > config.predict_tolerance && (deltaY < Math.min(estimatedVelocity, 0) || deltaY > Math.max(estimatedVelocity, 0)) && player.currentTick > 20) {
                        // Punish
                        this.punish(event, player, data, 0, Math.abs(discrepancy) * 5F,
                                "d:" + deltaY, "e:" + estimatedVelocity, "p:" + player.velocity.y, "t:l");
                    } else {
                        reward(0, data, 0.999);
                    }
                    if (e.isOnSlimeNext) {
                        data.estimatedVelocity = -estimatedVelocity;
                    } else {
                        data.estimatedVelocity = 0;
                    }
                } else if (e.updatePos && e.velocity.lengthSquared() > 0) {
                    if (Math.abs(discrepancy) > config.predict_tolerance && player.currentTick > 20) {
                        // Punish
                        this.punish(event, player, data, 0, Math.abs(discrepancy) * 5F,
                                "d:" + deltaY, "e:" + estimatedVelocity, "p:" + player.velocity.y, "t:a");
                    } else {
                        reward(0, data, 0.999);
                    }
                    data.estimatedVelocity = e.touchedBlocks.contains(MatUtils.COBWEB.parse()) ?
                            0 : estimatedVelocity;
                }
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

    private boolean inSpecialBlock(final Set<Material> collidingBlocks) {
        return collidingBlocks.contains(MatUtils.SCAFFOLDING.parse()) ||
                collidingBlocks.contains(MatUtils.KELP.parse()) ||
                collidingBlocks.contains(MatUtils.KELP_PLANT.parse()) ||
                collidingBlocks.contains(MatUtils.BUBBLE_COLUMN.parse()) ||
                collidingBlocks.contains(MatUtils.SWEET_BERRY_BUSH.parse()) ||
                collidingBlocks.stream().anyMatch(BlockUtils.SHULKER_BOX::contains);
    }

    private boolean inLadder(final Set<Material> collidingBlocks) {
        return collidingBlocks.contains(Material.LADDER) || collidingBlocks.contains(Material.VINE);
    }

    /**
     * Step check
     * <p>
     * Accuracy: 10/10 - Should not have any false positives.
     * Efficiency: 9/10 - Detects a lot of types of step instantly.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.stepLegitly || e.isTeleport || e.knockBack != null || inLadder(e.collidingBlocks) || e.isOnSlime || e.collidingEntities.contains(EntityType.BOAT)) {
                return;
            }
            double deltaY = e.velocity.y;

            if ((deltaY > 0.6 || deltaY < -0.0784) && e.onGround && player.onGround) {
                // Punish
                this.punish(event, player, data, 1, 4, "d:" + deltaY, "t:a");
            } else if (e.onGroundReally && Math.abs(player.prevPrevDeltaY - 0.333) < 0.01 &&
                    Math.abs(player.velocity.y - 0.248) < 0.01 && deltaY <= 0) {
                // Punish
                this.punish(event, player, data, 1, 3, "d:" + deltaY, "t:b");
            } else {
                reward(1, data, 0.99);
            }

            if (deltaY == 0) {
                data.safeLoc = e.to;
            }
        }
    }

    /**
     * Ability packet check.
     * <p>
     * Accuracy: 10/10 - Should not have any false positives.
     * Efficiency: 10/10 - Detects instantly.
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionNode config) {
        if (event instanceof AbilitiesEvent) {
            if (!player.getPlayer().getAllowFlight()) {
                // Punish
                this.punish(event, player, data, 2, 6);
            }
        }
    }
}