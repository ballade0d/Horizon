package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.Material;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.SpeedConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.SpeedData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.MathUtils;

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
        typeA(event, player, data, config);
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

            double prevSpeed;
            double speed;
            if (e.updatePos) {
                prevSpeed = data.prevSpeed;
                speed = MathUtils.distance2d(e.to.x - e.from.x, e.to.z - e.from.z);
            } else {
                speed = 0;
                prevSpeed = speed;
            }

            boolean flying = player.isFlying();
            // TODO: Swim Handler
            boolean swimming = false;
            boolean jump = e.jumpLegitly || (e.stepLegitly && player.isOnGround && player.isSprinting);

            float friction = e.oldFriction;
            float maxForce = this.computeMaxInputForce(player, e.newFriction);

            Set<Material> touchedBlocks = e.cube.add(-e.velocity.x, -e.velocity.y, -e.velocity.z).getMaterials(e.to.world);

            // TODO: Handle velocities

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
            if (touchedBlocks.contains(Material.SLIME_BLOCK) && Math.abs(player.velocity.y) < 0.1 && !player.isSneaking) {
                multipliers *= 0.4 + Math.abs(player.velocity.y) * 0.2;
            }

            double adders = 0;
            if (player.isSprinting && jump) {
                adders += 0.2;
            }

            double estimatedSpeed = friction * prevSpeed * multipliers + (maxForce + adders + 0.000001);

            double discrepancy;
            if (swimming && !flying) {
                // TODO: Liquid handler
                discrepancy = 0;
            } else {
                discrepancy = speed - estimatedSpeed;
            }
            if (e.updatePos) {
                if (discrepancy < 0 || speed > 0) {
                    data.discrepancies = Math.max(data.discrepancies + discrepancy, 0);
                }
                double totalDiscrepancy = data.discrepancies;
                if (discrepancy > 0 && totalDiscrepancy > 0.1) {
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
            float groundMultiplier = (5 * (player.player.getWalkSpeed()));
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