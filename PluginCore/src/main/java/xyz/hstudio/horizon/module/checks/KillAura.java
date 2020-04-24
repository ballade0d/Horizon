package xyz.hstudio.horizon.module.checks;

import org.bukkit.entity.LivingEntity;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.ActionEvent;
import xyz.hstudio.horizon.api.events.inbound.InteractEntityEvent;
import xyz.hstudio.horizon.api.events.inbound.InteractItemEvent;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.KillAuraData;
import xyz.hstudio.horizon.file.node.KillAuraNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.util.MathUtils;
import xyz.hstudio.horizon.util.wrap.AABB;
import xyz.hstudio.horizon.util.wrap.Ray;
import xyz.hstudio.horizon.util.wrap.Vector3D;

public class KillAura extends Module<KillAuraData, KillAuraNode> {

    private static final double EXPANDER = Math.pow(2, 24);

    public KillAura() {
        super(ModuleType.KillAura, new KillAuraNode(), "Order", "SuperKb", "GCD", "Direction", "InteractAutoBlock", "NormalAutoBlock", "Multi", "KeepSprint");
    }

    @Override
    public KillAuraData getData(final HoriPlayer player) {
        return player.killAuraData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        // TODO: Finish this
        if (type == 4 || type == 5) {
            McAccessor.INSTANCE.releaseItem(player.player);
            player.player.updateInventory();
        } else if (type == 0 || type == 1 || type == 6) {
            event.setCancelled(true);
        }
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (config.order_enabled) {
            typeA(event, player, data, config);
        }
        if (config.superkb_enabled) {
            typeB(event, player, data, config);
        }
        if (config.gcd_enabled) {
            typeC(event, player, data, config);
        }
        if (config.direction_enabled) {
            typeD(event, player, data, config);
        }
        if (config.interact_autoblock_enabled) {
            typeE(event, player, data, config);
        }
        if (config.normal_autoblock_enabled) {
            typeF(event, player, data, config);
        }
        if (config.multi_enabled) {
            typeG(event, player, data, config);
        }
        if (config.keep_sprint_enabled) {
            typeH(event, player, data, config);
        }
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            data.lastHitTick = player.currentTick;
        }
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
    private void typeA(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
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
                    reward(0, data, 0.999);
                }
                return;
            }
            if (++data.typeAFails > 5) {
                // Punish
                this.punish(event, player, data, 0, 4, "d:" + deltaT);
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
    private void typeB(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (event instanceof ActionEvent) {
            ActionEvent e = (ActionEvent) event;
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
                // Punish
                this.punish(event, player, data, 1, 5, "d:" + deltaT);
            } else {
                reward(1, data, 0.999);
            }
        }
    }

    /**
     * A GCD check.
     * It detects a large amount of killaura.
     * <p>
     * Accuracy: 7/10 - It may have false positives, though it only works when player fails other aura check
     * Efficiency: 9/10 - Detects a large amount of aura almost instantly
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!e.updateRot || e.isTeleport) {
                return;
            }
            // Only execute if player fails any other killaura checks.
            if (!config.gcd_strict && player.currentTick - data.lastFailTick > 5) {
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
                    // Punish
                    this.punish(event, player, data, 2, 3, "gcd:" + gcd);
                }
            } else if (data.gcdFails > 0) {
                data.gcdFails--;
            } else {
                reward(2, data, 0.999);
            }

            data.lastPitchChange = pitchChange;
        }
    }

    private long greatestCommonDivisor(final int current, final int previous) {
        return previous <= 32768 ? current : this.greatestCommonDivisor(previous, current % previous);
    }

    private float getDistance(final float from, final float to) {
        float distance = Math.abs(to - from) % 360F;
        return distance > 180 ? 360 - distance : distance;
    }

    /**
     * A direction check. It can detect a large amount of aura.
     * <p>
     * Accuracy: 7/10 - It has some false positives in some situation, will fix soon.
     * Efficiency: 9/10 - Detects all poorly made auras super fast
     *
     * @author MrCraftGoo
     */
    private void typeD(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (player.currentTick - data.lastHitTick > 10 || e.isTeleport) {
                return;
            }
            if (!e.strafeNormally) {
                if (++data.typeDFails > 3) {
                    // Punish
                    this.punish(event, player, data, 3, 3);
                }
            } else if (data.typeDFails > 0) {
                data.typeDFails--;
            } else {
                reward(3, data, 0.999);
            }
        }
    }

    /**
     * A Interact AutoBlock check. May also detect related hacks.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeE(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.INTERACT || e.intersection == null) {
                return;
            }
            if (!(e.entity instanceof LivingEntity)) {
                return;
            }
            data.intersection = e.intersection.clone().add(e.entity.getLocation().toVector());
        } else if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (data.intersection == null || player.getVehicle() != null || e.isTeleport || player.currentTick - data.lastHitTick > 2) {
                data.intersection = null;
                return;
            }

            Vector3D headPos = player.getHeadPosition();
            Vector3D dirA = MathUtils.getDirection(e.from.yaw, e.to.pitch);
            Vector3D dirB = data.intersection.subtract(headPos).normalize();

            if (dirA.dot(dirB) < 0) {
                // Punish
                this.punish(event, player, data, 4, 3);
            } else {
                reward(4, data, 0.99);
            }

            data.intersection = null;
        }
    }

    /**
     * A Normal AutoBlock check. May also detect related hacks.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeF(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (e.action != InteractEntityEvent.InteractType.ATTACK) {
                data.interactEntity = true;
                return;
            }
            data.interactEntity = false;
            AABB victimCube = McAccessor.INSTANCE.getCube(e.entity);

            Vector3D eyePos = player.getHeadPosition();
            Vector3D direction = MathUtils.getDirection(player.position.yaw, player.position.pitch);
            Ray ray = new Ray(eyePos, direction);

            Vector3D intersection = victimCube.intersectsRay(ray, 0, 3);

            if (intersection == null) {
                return;
            }

            data.lastIntersectTick = player.currentTick;
        } else if (event instanceof InteractItemEvent) {
            InteractItemEvent e = (InteractItemEvent) event;
            if (e.interactType != InteractItemEvent.InteractType.START_USE_ITEM) {
                return;
            }
            if (data.interactEntity) {
                return;
            }
            if (player.currentTick - data.lastIntersectTick < 2) {
                // Punish
                this.punish(event, player, data, 5, 3);
            } else {
                reward(5, data, 0.99);
            }
        }
    }

    /**
     * A MultiAura check.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positives
     * Efficiency: 10/10 - Super fast
     *
     * @author MrCraftGoo
     */
    private void typeG(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (event instanceof InteractEntityEvent) {
            InteractEntityEvent e = (InteractEntityEvent) event;
            if (player.protocol != 47 || e.action != InteractEntityEvent.InteractType.ATTACK) {
                return;
            }
            if (data.lastHitTick == player.currentTick && !e.entity.getUniqueId().equals(data.prevHit)) {
                // Punish
                this.punish(event, player, data, 6, 5);
            } else {
                reward(6, data, 0.99);
            }
            data.prevHit = e.entity.getUniqueId();
        }
    }

    /**
     * A KeepSprint check.
     * <p>
     * Accuracy: 8/10 - May have some falses.
     * Efficiency: 9/10 - Detects on second hit.
     *
     * @author FrozenAnarchy
     */
    private void typeH(final Event event, final HoriPlayer player, final KillAuraData data, final KillAuraNode config) {
        if (event instanceof MoveEvent) {
            // Player get slow down after 1 tick
            if (player.currentTick - player.hitSlowdownTick != 1) {
                return;
            }
            MoveEvent e = (MoveEvent) event;
            // Account for teleport, liquid, velocity, and air
            if (e.isTeleport || e.isInLiquidStrict || e.knockBack != null || !e.onGroundReally || !e.onGround) {
                return;
            }
            double difference = Math.abs(e.velocity.clone().setY(0).length() - player.velocity.clone().setY(0).length());

            if (difference < 0.027) {
                // Add a threshold to avoid some falses
                if (++data.keepSprintFails > 4) {
                    this.punish(event, player, data, 7, 2);
                }
            } else if (data.keepSprintFails > 0) {
                data.keepSprintFails--;
            } else {
                reward(7, data, 0.99);
            }
        }
    }
}