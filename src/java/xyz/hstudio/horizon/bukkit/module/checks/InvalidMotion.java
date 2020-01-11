package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.util.Vector;
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
        typeB(event, player, data, config);
    }

    /**
     * Basic y-axis motion check. Based on Minecraft moving mechanism.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updatePos || e.player.player.isFlying()) {
                return;
            }

            double deltaY = e.velocity.getY();

            // Changed prevDeltaY to lastExpect to avoid some weird bugs.
            double prevDeltaY = data.lastExpect;
            // Magic number from MCP.
            // Basically it's something like gravitational acceleration.
            double expected = (prevDeltaY - 0.08) * 0.98;

            // Trust client onGround?
            // TODO: Handle Water, Fake Ground, Velocity, Step, Tower, Vehicle
            // TODO: Fix the false positives when toggling fly.

            if (player.isGliding) {
                expected = prevDeltaY;
                float pitchRot = (float) Math.toRadians(e.to.pitch - e.from.pitch);
                // Magic numbers from MCP
                // Wtf is this??
                double magic = McAccess.getInst().cos(pitchRot);
                magic = magic * magic * Math.min(1, e.from.getDirection().length() / 0.4);
                if (prevDeltaY < 0) {
                    expected += prevDeltaY * -0.1 * magic;
                }
                if (pitchRot < 0) {
                    expected += e.velocity.length() * -McAccess.getInst().sin(pitchRot) * 0.04 * 3.2;
                }
                expected *= 0.99;
            } else {
                if (e.isOnSlime || e.isOnBed) {
                    // Ignore because isOnSlime and isOnBed is already predicated in MoveEvent
                    expected = deltaY;
                } else if (e.collidingBlocks.contains(MatUtils.COBWEB.parse())) {
                    // Y speed in web. Anything shouldn't affect it so it's a value.
                    expected = -0.007;
                } else if (e.onGround) {
                    expected = 0;
                    data.inAir = false;
                } else {
                    // This is updated after move, so in the tick player get/lose the potion this may false.
                    int levitation = player.getPotionEffectAmplifier("LEVITATION");
                    boolean onLadder = e.collidingBlocks.contains(MatUtils.LADDER.parse()) ||
                            e.collidingBlocks.contains(MatUtils.VINE.parse());
                    if (levitation > 0) {
                        expected = prevDeltaY + (0.05 * levitation - prevDeltaY) * 0.2;
                    } else if (!data.inAir && deltaY > 0) {
                        // Jump init height.
                        expected = 0.42 + player.getPotionEffectAmplifier("JUMP") * 0.1;
                        if (onLadder && MathUtils.abs(deltaY - 0.1176) < 0.001) {
                            expected = 0.1176;
                        }
                    } else if (onLadder) {
                        if (player.isSneaking) {
                            expected = 0;
                            // Players can jump even if they're colliding with ladder on ground,
                            // so I added this judgement.
                        } else if (MathUtils.abs(expected - deltaY) > 0.001) {
                            expected = deltaY < 0 ? -0.15 : 0.1176;
                        }
                    }
                    data.inAir = true;
                }
            }
            // Fix a weird stuff that in 1.8 small movements are approximated to 0.
            if (MathUtils.abs(expected) < 0.005) {
                expected = deltaY;
            }

            // Check for absolute value so that FastFall/Hop hacks will also be detected.
            double discrepancy = MathUtils.abs(deltaY - expected);
            // Don't check if onGroundReally is true to avoid false positives when joining.
            if (!e.onGround && !e.onGroundReally && discrepancy > config.tolerance) {
                this.debug("Failed: TypeA, d:" + deltaY + ", e:" + expected);

                // Punish
                this.punish(player, data, "TypeA", 0);
            }

            data.lastExpect = expected;
        }
    }

    /**
     * An easy Strafe/Speed/InvalidMotion check.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updatePos || e.player.player.isFlying()) {
                return;
            }
            if (!e.collidingBlocks.isEmpty() || e.onGround || player.isOnGround) {
                return;
            }
            Vector velocity = e.velocity.clone().setY(0);
            Vector prevVelocity = player.velocity.clone().setY(0);
            double speed = velocity.lengthSquared();
            double angle = MathUtils.angle(velocity, prevVelocity);
            if (speed > 0.015 && angle > 0.2) {
                this.debug("Failed: TypeB, s:" + speed + ", a:" + angle);

                // Punish
                this.punish(player, data, "TypeB", 0);
            }
        }
    }
}