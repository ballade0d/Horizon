package xyz.hstudio.horizon.bukkit.module.checks;

import xyz.hstudio.horizon.bukkit.compat.McAccessor;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;

public class Speed {

    /**
     * Get the max input force.
     * Combined Islandscout's code and mine and MCP (Much lighter after editing).
     *
     * @author Islandscout, MrCraftGoo
     */
    private float getMaxInputForce(final HoriPlayer player, final float newFriction) {
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
            float movementFactor = (float) McAccessor.INSTANCE.getMoveFactor(player.player);
            multiplier = movementFactor * 0.16277136F / (newFriction * newFriction * newFriction);
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
        return (finalForce * (sprinting && flying ? 2 : 1));
    }
}