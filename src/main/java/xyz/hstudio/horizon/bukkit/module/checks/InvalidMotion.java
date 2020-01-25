package xyz.hstudio.horizon.bukkit.module.checks;

import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
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
    }

    /**
     * Basic y-axis motion check. Based on Minecraft moving mechanism.
     * <p>
     * Accuracy: 8/10 - It may have some false positives.
     * Efficiency: 10/10 - Detects a lot of move related hacks instantly
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updatePos || player.isFlying()) {
                return;
            }

            double deltaY = e.velocity.getY();

            // Use lastExpect to avoid some weird bugs.
            double prevDeltaY = data.lastExpect;
            // Magic number from MCP.
            // Basically it's something like gravitational acceleration.
            double expected = (prevDeltaY - 0.08) * 0.98;

            // TODO: Handle Water, Fake Ground, Velocity, Step, Tower, Vehicle

            if (player.isGliding) {
                // Gliding function, from MCP
                double motionY = player.prevDeltaY;
                float pitchRot = (float) Math.toRadians(e.to.pitch);
                double magic = McAccess.getInst().cos(pitchRot);
                magic = magic * magic * Math.min(1.0, e.to.getDirection().length() / 0.4);
                motionY += -0.08 + magic * 0.06;
                if (motionY < 0) {
                    motionY += motionY * -0.1 * magic;
                }
                if (pitchRot < 0) {
                    motionY += player.velocity.clone().setY(0).length() * (-McAccess.getInst().sin(pitchRot)) * 0.04 * 3.2;
                }
                motionY *= 0.98;
                expected = motionY;
            } else {
                if (e.isOnSlime || e.isOnBed) {
                    // Ignore because it is already predicated in MoveEvent
                    expected = deltaY;
                    data.inAir = true;
                } else if (e.collidingBlocks.contains(MatUtils.COBWEB.parse())) {
                    // Y speed in web. Anything shouldn't affect it so it's a value.
                    // TODO: Fix the false positives when moving out from cobweb.
                    expected = -0.00196;
                } else if (e.onGround || e.onGroundReally) {
                    // Have to be 0 because of some client bugs.
                    expected = 0;
                    data.inAir = false;
                } else {
                    // This is updated after move, so in the tick player get/lose the potion this may false.
                    int levitation = player.getPotionEffectAmplifier("LEVITATION");
                    boolean onLadder = e.collidingBlocks.contains(MatUtils.LADDER.parse()) ||
                            e.collidingBlocks.contains(MatUtils.VINE.parse());
                    if (levitation > 0) {
                        // Levitation function
                        expected = prevDeltaY + (0.05 * levitation - prevDeltaY) * 0.2;
                    } else if (!data.inAir && deltaY > 0) {
                        // Jump init height.
                        expected = 0.42 + player.getPotionEffectAmplifier("JUMP") * 0.1;
                        // The first tick climbing ladder
                        if (onLadder && MathUtils.abs(deltaY - 0.1176) < 0.001) {
                            expected = 0.1176;
                        }
                    } else if (onLadder) {
                        if (player.isSneaking) {
                            expected = 0;
                            // Players can jump even if they're colliding with ladder on ground,
                            // so I added this judgement.
                        } else if (MathUtils.abs(expected - deltaY) > 0.001) {
                            // -0.15 = max climbing down speed
                            // 0.1176 = max climbing up speed (0.1176 = (0.2 - 0.08) * 0.98)
                            expected = deltaY < 0 ? -0.15 : 0.1176;
                        }
                    }
                    data.inAir = true;
                }
            }
            // In 1.8.8 or lower, small movements are approximated to 0.
            if (MathUtils.abs(expected) < 0.005 && expected != 0) {
                expected = deltaY;
            }
            if (e.isTeleport) {
                expected = 0;
            }

            // Check for absolute value so that FastFall and LowHop will also be detected.
            // But 1.9+ is weird and some functions are not very accurate so absolute value can't be used.
            double discrepancy = deltaY - expected;
            // Don't check if onGroundReally is true to avoid false positives when chunks aren't sent to the player.
            // Don't check if onGround is true to avoid some false positives. GroundSpoof check will fix the bypass.
            if (!e.isTeleport && !e.onGround && !e.onGroundReally && discrepancy > config.typeA_tolerance) {
                this.debug("Failed: TypeA, d:" + deltaY + ", e:" + expected + ", p:" + prevDeltaY);

                // Punish
                this.punish(player, data, "TypeA", discrepancy * 5);
            } else {
                reward("TypeA", data, 0.99);
            }

            data.lastExpect = expected;
        }
    }
}