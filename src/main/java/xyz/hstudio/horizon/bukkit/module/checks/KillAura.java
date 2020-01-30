package xyz.hstudio.horizon.bukkit.module.checks;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.compat.McAccessor;
import xyz.hstudio.horizon.bukkit.config.checks.KillAuraConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.KillAuraData;
import xyz.hstudio.horizon.bukkit.learning.core.KnnClassification;
import xyz.hstudio.horizon.bukkit.learning.KnnVectorClassification;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.ActionEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;
import xyz.hstudio.horizon.bukkit.util.AABB;
import xyz.hstudio.horizon.bukkit.util.MathUtils;
import xyz.hstudio.horizon.bukkit.util.Ray;

public class KillAura extends Module<KillAuraData, KillAuraConfig> {

    private static final double EXPANDER = Math.pow(2, 24);

    private final KnnClassification<Vector> classification;

    public KillAura() {
        super(ModuleType.KillAura, new KillAuraConfig());
        this.classification = new KnnVectorClassification();
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
     * A SuperKnockBack check.
     * It should detect all SuperKb.
     * <p>
     * Accuracy: 10/10 - Should not have any false positives
     * Efficiency: 10/10 - Detects superKb instantly
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof ActionEvent) {
            ActionEvent e = (ActionEvent) event;
            // Don't need to skip 1.9+
            // because players can't start/stop sprinting while standing still
            switch (e.action) {
                case START_SPRINTING:
                    data.startSprintTick = player.currentTick;
                    break;
                case STOP_SPRINTING:
                    data.failTypeBTick = data.startSprintTick;
                    break;
                default:
                    break;
            }
        } else if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            long deltaT = player.currentTick - data.failTypeBTick;
            if (deltaT == 0) {
                this.debug("Failed: TypeB, d:" + deltaT);

                // Punish
                this.punish(player, data, "TypeB", 5);
            } else {
                reward("TypeB", data, 0.999);
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
    private void typeC(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
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
                    this.debug("Failed: TypeC, g:" + gcd);

                    // Punish
                    this.punish(player, data, "TypeC", 3);
                }
            } else if (data.gcdFails > 0) {
                data.gcdFails--;
            } else {
                reward("TypeC", data, 0.999);
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
     * A direction check. It can detect a large amount of killaura.
     * <p>
     * Accuracy: 7/10 - It may have some false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeD(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (player.currentTick - data.lastHitTick > 5 || e.isTeleport) {
                return;
            }
            // TODO: Add a threshold
            if (!e.strafeNormally) {
                this.debug("Failed: TypeD");

                // Punish
                this.punish(player, data, "TypeD", 4);
            } else {
                reward("TypeD", data, 0.999);
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
    private void typeE(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updateRot) {
                return;
            }
            float diffYaw = MathUtils.abs(e.to.yaw - e.from.yaw);
            float diffPitch = MathUtils.abs(e.to.pitch - e.from.pitch);
            if (diffYaw >= 3.0 && diffPitch > 0.001 && diffPitch < 0.0995) {
                this.debug("Failed: TypeE, p:1");

                // Punish
                this.punish(player, data, "TypeE", 5);
            }
        }
    }

    /**
     * Machine Learning check.
     * TODO: Finish
     *
     * @author MrCraftGoo
     */
    private void typeF(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraConfig config) {
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            Entity victim = e.entity;
            AABB victimAABB = McAccessor.INSTANCE.getCube(victim);

            Vector eyePos = player.getHeadPosition();
            Vector direction = MathUtils.getDirection(player.position.yaw, player.position.pitch);
            Ray ray = new Ray(eyePos, direction);

            Vector intersection = victimAABB.intersectsRay(ray, 0, Float.MAX_VALUE);
            if (intersection == null) {
                return;
            }
            intersection.subtract(victim.getLocation().toVector());
            //
        }
    }
}