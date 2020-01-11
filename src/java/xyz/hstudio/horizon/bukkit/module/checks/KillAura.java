package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.KillAuraConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.KillAuraData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.*;
import xyz.hstudio.horizon.bukkit.util.MathUtils;
import xyz.hstudio.horizon.bukkit.util.TimeUtils;

public class KillAura extends Module<KillAuraData, KillAuraConfig> {

    private static final double EXPANDER = Math.pow(2, 24);

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
        typeF(event, player, data, config);
    }

    /**
     * An easy packet order check.
     * This will detect all post killaura,
     * Yes, it's like 10 lines.
     * <p>
     * Accuracy: 8/10 - Should not have much false positives
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
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
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
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
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
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            long deltaT = player.currentTick - data.failTypeCTick;
            if (deltaT == 0) {
                this.debug("Failed: TypeC, d:" + deltaT);

                // Punish
                this.punish(player, data, "TypeC", 0);
            }
        }
    }

    /**
     * A GCD check.
     * It detects a large amount of killaura.
     * <p>
     * Accuracy: 7/10 - It may have false positives, though it only works when player fails other killaura check
     * Efficiency: 9/10 - Almost instantly
     *
     * @author MrCraftGoo
     */
    private void typeD(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updateRot) {
                return;
            }
            // Only execute if player fails any other killaura checks.
            if (player.currentTick - data.lastFailTick > 5) {
                return;
            }
            float pitchChange = this.getDistance(e.from.pitch, e.to.pitch);
            float yawChange = this.getDistance(e.from.yaw, e.to.yaw);
            if (pitchChange == 0 || yawChange == 0) {
                data.lastPitchChange = pitchChange;
                return;
            }
            // Convert it to int so I can get gcd.
            int pitch = (int) (pitchChange * EXPANDER);
            int lastPitch = (int) (data.lastPitchChange * EXPANDER);
            long gcd = this.greatestCommonDivisor(pitch, lastPitch);

            if (gcd <= 131072) {
                this.debug("Failed: TypeD, g:" + gcd);

                // Punish
                this.punish(player, data, "TypeD", 0);
            }

            data.lastPitchChange = pitchChange;
        }
    }

    private long greatestCommonDivisor(final int current, final int previous) {
        return previous <= 16384 ? current : this.greatestCommonDivisor(previous, current % previous);
    }

    private float getDistance(final float from, final float to) {
        float distance = MathUtils.abs(to - from) % 360F;
        return distance > 180 ? 360 - distance : distance;
    }

    /**
     * An AutoClicker check based on client tick.
     * <p>
     * Accuracy: 9/10 - Haven't found any false positives
     * Efficiency: 6/10 - A bit slow
     *
     * @author MrCraftGoo
     */
    private void typeE(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            if (!data.swung) {
                data.moves++;
                return;
            }
            if (data.moves < 8 && data.moveInterval.add(data.moves) && data.moveInterval.size() == 25) {
                double average = data.moveInterval.stream()
                        .mapToDouble(d -> d)
                        .average()
                        .orElse(0);
                double stdDeviation = 0;
                for (int i : data.moveInterval) {
                    stdDeviation += NumberConversions.square(i - average);
                }

                // Customizable?
                if (stdDeviation < 0.3) {
                    this.debug("Failed: TypeE, s:" + stdDeviation);

                    // Punish
                    this.punish(player, data, "TypeE", 0);
                }
                data.moveInterval.clear();
            }
            data.swung = false;
            data.moves = 1;
        } else if (event instanceof SwingEvent) {
            data.swung = true;
        }
    }

    /**
     * A direction check. It can detect a large amount of killaura.
     * I learnt this from Islandscout, but much lighter than his.
     * <p>
     * TODO: Ignore when colliding entities
     * TODO: Ignore the first tick player jump (Unimportant)
     * TODO: Ignore when getting knock back
     * <p>
     * Accuracy: 7/10 - It may have some false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author Islandscout, MrCraftGoo
     */
    private void typeF(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updateRot || player.currentTick - data.lastHitTick > 6 || player.vehicle != -1 || e.isUnderBlock || !e.collidingBlocks.isEmpty()) {
                return;
            }

            Block footBlock = player.position.add(0, -1, 0).getBlock();
            if (footBlock == null) {
                return;
            }
            Vector velocity = e.velocity.clone().setY(0);
            double friction = e.oldFriction;
            Vector prevVelocity = player.velocity.clone();
            if (e.hitSlowdown) {
                prevVelocity.multiply(0.6);
            }
            if (MathUtils.abs(prevVelocity.getX() * friction) < 0.005) {
                prevVelocity.setX(0);
            }
            if (MathUtils.abs(prevVelocity.getZ() * friction) < 0.005) {
                prevVelocity.setZ(0);
            }
            double dX = velocity.getX();
            double dZ = velocity.getZ();
            dX /= friction;
            dZ /= friction;
            dX -= prevVelocity.getX();
            dZ -= prevVelocity.getZ();

            Vector accelDir = new Vector(dX, 0, dZ);
            Vector yaw = MathUtils.getDirection(e.to.yaw, 0);

            if (velocity.length() < 0.15 || accelDir.lengthSquared() < 0.000001) {
                return;
            }

            boolean vectorDir = accelDir.clone().crossProduct(yaw).dot(new Vector(0, 1, 0)) >= 0;
            double angle = (vectorDir ? 1 : -1) * MathUtils.angle(accelDir, yaw);

            double multiple = angle / (Math.PI / 4);
            double threshold = Math.toRadians(config.max_angle);

            // TODO: Add a threshold
            if (MathUtils.abs(multiple - NumberConversions.floor(multiple)) > threshold && MathUtils.abs(multiple - NumberConversions.ceil(multiple)) > threshold) {
                this.debug("Failed: TypeF, a:" + angle);

                // Punish
                this.punish(player, data, "TypeF", 0);
            }
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            data.lastHitTick = player.currentTick;
        }
    }

    /**
     * A HitBox check.
     */
    private void typeG(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {

    }
}