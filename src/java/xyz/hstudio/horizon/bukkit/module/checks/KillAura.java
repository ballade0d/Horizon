package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.KillAuraConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.KillAuraData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.ActionEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractItemEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.MathUtils;
import xyz.hstudio.horizon.bukkit.util.TimeUtils;

public class KillAura extends Module<KillAuraData, KillAuraConfig> {

    private final double EXPANDER = Math.pow(2, 24);

    public KillAura() {
        super(ModuleType.KillAura, new KillAuraConfig());
    }

    @Override
    public KillAuraData getData(final HoriPlayer player) {
        return player.killAuraData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        typeA(event, player, data, config);
        typeB(event, player, data, config);
        typeC(event, player, data, config);
        typeD(event, player, data, config);
        typeE(event, player, data, config);
    }

    /**
     * An easy packet order check.
     * This will detect all post killaura,
     * Yes, it's like 10 lines.
     * <p>
     * Accuracy: 7/10 - Should not have much false positives
     * Efficiency: 9/10 - Detects post killaura almost instantly
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            long now = TimeUtils.now();
            data.lagging = now - data.lastMove < 5;
            data.lastMove = now;
        } else if (event instanceof InteractEntityEvent) {
            // Ignoring laggy players so this check won't false af like matrix while lagging
            // Inspired by funkemonkey
            long deltaT = TimeUtils.now() - data.lastMove;
            if (data.lagging || deltaT >= 20) {
                if (data.typeAFails > 0) {
                    data.typeAFails--;
                }
                return;
            }
            if (++data.typeAFails > 5) {
                this.debug("Failed: TypeA, d:" + deltaT);

                // Punish
                this.punish(player, data, "TypeA", 0);
            }
        }
    }

    /**
     * An AutoBlock check.
     * This will detect most autoblock, probably all.
     * <p>
     * Accuracy: 10/10 - Should not have any false positives
     * Efficiency: 10/10 - Detects autoblock instantly
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof InteractItemEvent) {
            InteractItemEvent e = (InteractItemEvent) event;
            ItemStack item = e.itemStack;
            // Check if the item can be used to block
            if (item == null || EnchantmentTarget.WEAPON.includes(item)) {
                // TODO: Remember to skip 1.9+ because client tick disappeared
                return;
            }
            switch (e.interactType) {
                case START_USE_ITEM: {
                    data.startBlockTick = player.currentTick;
                }
                case RELEASE_USE_ITEM: {
                    data.failTypeBTick = data.startBlockTick;
                }
            }
        } else if (event instanceof InteractEntityEvent) {
            long deltaT = player.currentTick - data.failTypeBTick;
            // Detect both pre AutoBlock and post AutoBlock.
            if (deltaT <= 1) {
                this.debug("Failed: TypeB, t:" + deltaT);

                // Punish
                this.punish(player, data, "TypeB", 0);
            }
        }
    }

    /**
     * A SuperKnockBack check.
     * It should detect all SuperKb.
     * <p>
     * Accuracy: 10/10 - Should not have any false positives
     * Efficiency: 10/10 - Detects superKb instantly
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof ActionEvent) {
            ActionEvent e = (ActionEvent) event;
            // Don't need to skip 1.9+
            // because players can't start/stop sprinting while standing still
            switch (e.action) {
                case START_SPRINTING: {
                    data.startSprintTick = player.currentTick;
                }
                case STOP_SPRINTING: {
                    data.failTypeCTick = data.startSprintTick;
                }
            }
        } else if (event instanceof InteractEntityEvent) {
            long deltaT = player.currentTick - data.failTypeCTick;
            if (deltaT == 0) {
                this.debug("Failed: TypeC, d:" + deltaT);

                // Punish
                this.punish(player, data, "TypeC", 0);
            }
        }
    }

    /**
     * A Rotation check.
     * It detects some aimbot and killaura.
     * From Islandscout, I'll credit him.
     * <p>
     * Accuracy: 9/10 - Haven't found any false positives
     * Efficiency: 3/10 - Very slow
     *
     * @author Islandscout
     */
    private void typeD(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            Vector mouseMove = new Vector(e.to.getYaw() - e.from.getYaw(), e.to.getPitch() - e.from.getPitch(), 0);
            data.mouseMoves.add(mouseMove);
            if (data.mouseMoves.size() > 5) {
                data.mouseMoves.remove(0);
            }

            if (!clickedBefore(player, data)) {
                return;
            }

            double minSpeed = Double.MAX_VALUE;
            double maxSpeed = 0D;
            double maxAngle = 0D;
            for (int i = 1; i < data.mouseMoves.size(); i++) {
                Vector lastMouseMove = data.mouseMoves.get(i - 1);
                Vector currMouseMove = data.mouseMoves.get(i);
                double speed = currMouseMove.length();
                double lastSpeed = lastMouseMove.length();
                double angle = (lastSpeed != 0 && lastSpeed != 0) ? MathUtils.angle(lastMouseMove, currMouseMove) : 0D;
                if (Double.isNaN(angle)) {
                    angle = 0D;
                }
                maxSpeed = Math.max(speed, maxSpeed);
                minSpeed = Math.min(speed, minSpeed);
                maxAngle = Math.max(angle, maxAngle);

                if (maxSpeed - minSpeed > 4 && minSpeed < 0.01 && maxAngle < 0.1 && lastSpeed > 1) {
                    this.debug("Failed: TypeD, ms:" + maxSpeed + ", ms:" + minSpeed + ", ma:" + maxAngle + ", ls:" + lastSpeed);

                    // Punish
                    this.punish(player, data, "TypeD", 0);
                }
            }
        } else if (event instanceof InteractEntityEvent) {
            long currTick = player.currentTick;
            if (!data.clickTicks.contains(currTick)) {
                data.clickTicks.add(currTick);
            }
            for (int i = data.clickTicks.size() - 1; i >= 0; i--) {
                if (currTick - data.clickTicks.get(i) > 5) {
                    data.clickTicks.remove(i);
                }
            }
        }
    }

    private boolean clickedBefore(final HoriPlayer player, final KillAuraData data) {
        long time = player.currentTick - 2;
        for (int i = 0; i < data.clickTicks.size(); i++) {
            if (time == data.clickTicks.get(i)) {
                data.clickTicks.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * A GCD check.
     * It detects a large amount of killaura.
     * <p>
     * Accuracy: 7/10 - It may have false positives, though it only works when player fails other killaura check.
     * Efficiency: 9/10 - Almost instantly
     *
     * @author MrCraftGoo
     */
    private void typeE(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updateRot) {
                return;
            }
            // Only execute if player fails any other killaura checks.
            if (player.currentTick - data.lastFailTick > 5) {
                return;
            }
            float pitchChange = Math.abs(e.to.getPitch() - e.from.getPitch());
            if (pitchChange == 0) {
                return;
            }
            int pitch = (int) (pitchChange * EXPANDER);
            int lastPitch = (int) (data.lastPitchChange * EXPANDER);
            long gcd = this.greatestCommonDivisor(pitch, lastPitch);
            if (gcd <= 0b100000000000000000) {
                this.debug("Failed: TypeE, g:" + gcd);

                // Punish
                this.punish(player, data, "TypeE", 0);
            }
            data.lastPitchChange = pitchChange;
        }
    }

    private long greatestCommonDivisor(final int current, final int previous) {
        return previous <= 16384 ? current : this.greatestCommonDivisor(previous, current % previous);
    }
}