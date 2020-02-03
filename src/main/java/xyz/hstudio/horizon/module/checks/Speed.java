package xyz.hstudio.horizon.module.checks;

import org.bukkit.Material;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.config.checks.SpeedConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.SpeedData;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.network.events.Event;
import xyz.hstudio.horizon.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.util.MathUtils;
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
    public void doCheck(final Event event, final HoriPlayer player, final SpeedData data, final SpeedConfig config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
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

            double prevSpeed = data.prevSpeed;
            double speed = e.updatePos ? MathUtils.distance2d(e.to.x - e.from.x, e.to.z - e.from.z) : 0;

            if (e.knockBack != null) {
                data.prevSpeed = speed;
                return;
            }

            boolean flying = player.isFlying();
            // TODO: Swim Handler
            boolean swimming = player.isInLiquid1_8;

            double estimatedSpeed;

            double discrepancy;
            if (swimming && !flying) {
                Vector3D move = e.velocity.clone().setY(0);
                Vector3D waterForce = e.waterFlowForce.clone().setY(0).normalize().multiply(0.014);
                double waterForceLength = waterForce.length();
                double computedForce = McAccessor.INSTANCE.cos(move.angle(waterForce)) * waterForceLength + 0.003;

                estimatedSpeed = this.waterMapping(prevSpeed, computedForce,
                        player.getEnchantmentEffectAmplifier("DEPTH_STRIDER"), player.isSprinting, e.onGround);
            } else {
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
                if (touchedBlocks.contains(Material.WEB)) {
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
                    this.punish(player, data, "TypeA", totalDiscrepancy * 10);
                    data.discrepancies = 0;
                } else {
                    reward("TypeA", data, 0.99);
                }
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
}