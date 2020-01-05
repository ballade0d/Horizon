package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.compat.McAccess;
import xyz.hstudio.horizon.bukkit.config.checks.InvalidMotionConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.InvalidMotionData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.MaterialUtils;

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
     *
     * @author MrCraftGoo, Islandscout
     */
    private void typeA(final Event event, final HoriPlayer player, final InvalidMotionData data, final InvalidMotionConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updatePos) {
                return;
            }
            Block standing = e.from.add(0, -0.01, 0).getBlock();
            if (standing == null) {
                return;
            }

            double deltaY = e.to.y - e.from.y;

            double prevDeltaY = data.prevDeltaY;
            double expected;

            // Trust client onGround?
            // TODO: Handle Honey, Water, FakeGround, Knockback, Step

            if (player.isGliding) {
                if (e.onGround) {
                    player.isGliding = false;
                    // Experiment value
                    expected = deltaY;
                } else {
                    double motionY = prevDeltaY;
                    Vector direction = e.from.getDirection();
                    float rotPitch = (float) Math.toRadians(e.to.pitch - e.from.pitch);
                    // Magic numbers from MCP
                    // Wtf is this??
                    float magic = McAccess.getInst().cos(rotPitch);
                    magic = (float) (magic * magic * Math.min(1.0D, direction.length() / 0.4D));
                    if (prevDeltaY < 0.0) {
                        motionY += prevDeltaY * -0.1 * (double) magic;
                    }
                    if (rotPitch < 0.0) {
                        motionY += e.velocity.length() * (-McAccess.getInst().sin(rotPitch)) * 0.04 * 3.2D;
                    }
                    motionY *= 0.9900000095367432D;
                    expected = motionY;
                }
            } else {
                // This magic value is something like elasticity.
                // Put these judgments in MoveEvent?
                double slimeExpect = -0.96 * data.prevPrevDeltaY;
                double bedExpect = -0.62F * data.prevPrevDeltaY;
                if (standing.getType() == MaterialUtils.SLIME_BLOCK.parse() && !player.isSneaking &&
                        data.prevDeltaY < 0 && deltaY > 0 && deltaY > (data.prevPrevDeltaY < -0.1F ? slimeExpect - 0.003 : 0) && deltaY <= slimeExpect) {
                    // Player is bouncing on slime
                    expected = deltaY;
                } else if (standing.getType().name().contains("BED") && !player.isSneaking &&
                        data.prevDeltaY < 0 && deltaY > 0 && deltaY > (data.prevPrevDeltaY < -0.1F ? bedExpect - 0.003 : 0) && deltaY <= bedExpect) {
                    // Player is bouncing on bed
                    expected = deltaY;
                } else if (e.collidingBlocks.contains(MaterialUtils.COBWEB.parse())) {
                    // Y speed in web. Anything shouldn't affect it so it's a value.
                    expected = -0.007;
                } else if (e.onGround) {
                    // expected = 0;
                    expected = deltaY;
                    data.inAir = false;
                } else {
                    if (!data.inAir) {
                        // Jump init height.
                        expected = 0.42 + player.getPotionEffectAmplifier("JUMP") * 0.1;
                    } else {
                        // Magic number from MCP
                        // Basically it's something like gravitational acceleration
                        expected = (prevDeltaY - 0.08) * 0.9800000190734863;
                    }
                    data.inAir = true;
                }
            }

            // Is 0.001 too strict?
            if (deltaY - expected > config.tolerance) {
                this.debug("Failed: TypeA, d:" + deltaY + ", e:" + expected);

                // Punish
                this.punish(player, data, "TypeA", 0);
            }

            data.prevPrevDeltaY = data.prevDeltaY;
            data.prevDeltaY = deltaY;
        }
    }
}