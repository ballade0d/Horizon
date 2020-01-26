package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.KillAuraConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.KillAuraData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.*;
import xyz.hstudio.horizon.bukkit.util.MathUtils;

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
        typeG(event, player, data, config);
        // TODO: Aim checks.
    }

    /**
     * A hit packet order check.
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
            long now = System.currentTimeMillis();
            data.lagging = now - data.lastMove < 5;
            data.lastMove = now;
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            // Ignoring laggy players so this check won't false af like matrix while lagging
            // Inspired by funkemonkey
            long deltaT = System.currentTimeMillis() - data.lastMove;
            if (data.lagging || deltaT >= 20) {
                if (data.typeAFails > 0) {
                    data.typeAFails--;
                } else {
                    reward("TypeA", data, 0.999);
                }
                return;
            }
            if (++data.typeAFails > 5) {
                this.debug("Failed: TypeA, d:" + deltaT);

                // Punish
                this.punish(player, data, "TypeA", 4);
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
                return;
            }
            // Use time instead of client tick because client tick disappeared in 1.9+.
            switch (e.interactType) {
                case START_USE_ITEM:
                    data.startBlockTime = System.currentTimeMillis();
                    break;
                case RELEASE_USE_ITEM:
                    data.failTypeBTime = data.startBlockTime;
                    break;
                default:
                    break;
            }
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            long deltaT = System.currentTimeMillis() - data.failTypeBTime;
            // Detect both pre and post AutoBlock.
            if (!data.lagging && deltaT <= 50) {
                this.debug("Failed: TypeB, t:" + deltaT);

                // Punish
                this.punish(player, data, "TypeB", 3);
            } else {
                reward("TypeB", data, 0.999);
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
                case START_SPRINTING:
                    data.startSprintTick = player.currentTick;
                    break;
                case STOP_SPRINTING:
                    data.failTypeCTick = data.startSprintTick;
                    break;
                default:
                    break;
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
                this.punish(player, data, "TypeC", 5);
            } else {
                reward("TypeC", data, 0.999);
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
                if (++data.gcdFails > 5) {
                    this.debug("Failed: TypeD, g:" + gcd);

                    // Punish
                    this.punish(player, data, "TypeD", 3);
                }
            } else if (data.gcdFails > 0) {
                data.gcdFails--;
            } else {
                reward("TypeD", data, 0.999);
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
        // TODO: Recode this
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

                // TODO: Customizable?
                if (stdDeviation < 0.3) {
                    this.debug("Failed: TypeE, s:" + stdDeviation);

                    // Punish
                    this.punish(player, data, "TypeE", 5);
                } else {
                    reward("TypeE", data, 0.999);
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
     * <p>
     * Accuracy: 7/10 - It may have some false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeF(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (player.currentTick - data.lastHitTick > 6 || e.isTeleport) {
                return;
            }
            // TODO: Add a threshold
            if (!e.strafeNormally) {
                this.debug("Failed: TypeF");

                // Punish
                this.punish(player, data, "TypeF", 4);
            } else {
                reward("TypeF", data, 0.999);
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
     * An AimBot/Rotation Pattern check.
     *
     * @author MrCraftGoo
     */
    private void typeG(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updateRot) {
                return;
            }
            float diffYaw = MathUtils.abs(e.to.yaw - e.from.yaw);
            float diffPitch = MathUtils.abs(e.to.pitch - e.from.pitch);
            if (diffYaw >= 3.0 && diffPitch > 0.001 && diffPitch < 0.0995) {
                this.debug("Failed: TypeG, p:1");

                // Punish
                this.punish(player, data, "TypeG", 5);
            }
        }
    }
}