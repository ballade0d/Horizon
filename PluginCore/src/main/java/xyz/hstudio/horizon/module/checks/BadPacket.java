package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.*;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.BadPacketData;
import xyz.hstudio.horizon.file.node.BadPacketNode;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;

public class BadPacket extends Module<BadPacketData, BadPacketNode> {

    public BadPacket() {
        super(ModuleType.BadPacket, new BadPacketNode(), "TypeA", "TypeB", "TypeC", "TypeD", "TypeE", "TypeF");
    }

    @Override
    public BadPacketData getData(final HoriPlayer player) {
        return player.badPacketData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final BadPacketData data, final BadPacketNode config) {
        if (type == 1 || type == 2) {
            event.setCancelled(true);
        } else if (type == 3) {
            event.setCancelled(true);
            Sync.teleport(player, data.legitLocation);
        } else if (type == 4) {
            McAccessor.INSTANCE.releaseItem(player.getPlayer());
            player.getPlayer().updateInventory();
        }
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
        if (config.typeB_enabled) {
            typeB(event, player, data, config);
        }
        if (config.typeC_enabled) {
            typeC(event, player, data, config);
        }
        if (config.typeD_enabled) {
            typeD(event, player, data, config);
        }
        if (config.typeE_enabled) {
            typeE(event, player, data, config);
        }
    }

    /**
     * A FreeCam/Freeze check. It can detect players from not sending movement packet.
     * <p>
     * Accuracy: 10/10 - Should not have any false positive.
     * Efficiency: 8/10 - Detects related hacks fast.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketNode config) {
        if (event instanceof KeepAliveRespondEvent) {
            if (player.getPlayer().isDead() || player.currentTick < 20 || player.teleports.size() > 0) {
                return;
            }
            // A player should send 20 move packets(1.8.8 or lower)
            // or 1 move packet (1.9 or higher) at least in one second.
            // If player didn't send move packets in 2 sec (KeepAlive Interval)
            // but send KeepAlive packets, the player is definitely cheating.
            if (player.currentTick == data.lastTick) {
                // Punish
                this.punish(event, player, data, 0, 5);
            } else {
                reward(0, data, 0.99);
            }
            data.lastTick = player.currentTick;
        }
    }

    /**
     * A Paralyze/MoveExploit check.
     * <p>
     * Accuracy: 10/10 - Should not have any false positive.
     * Efficiency: 9/10 - Detects related hacks really fast.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.moveType != MoveEvent.MoveType.FLYING || e.isTeleport || player.teleports.size() > 0 ||
                    System.currentTimeMillis() - player.lastTeleportAcceptTick < 1000L) {
                data.flyingCount = 0;
                return;
            }
            // A player can send only 19 flying packets in a row.
            if (++data.flyingCount > 21) {
                // Punish
                this.punish(event, player, data, 1, 5);
            } else if (data.flyingCount == 0) {
                reward(1, data, 0.99);
            }
        }
    }

    /**
     * A CustomPayload ServerCrasher check.
     * <p>
     * Accuracy: 10/10 - Should not have any false positive.
     * Efficiency: 10/10 - Detects related hacks really fast.
     *
     * @author MrCraftGoo
     */
    private void typeC(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketNode config) {
        if (event instanceof CustomPayloadEvent) {
            CustomPayloadEvent e = (CustomPayloadEvent) event;

            String brand = e.brand;
            // Only checks for these 2 brands
            if (!"MC|BEdit".equalsIgnoreCase(brand) &&
                    !"MC|BSign".equalsIgnoreCase(brand)) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now - data.lastPayloadTime <= e.length) {
                // Punish
                this.punish(event, player, data, 2, 5);
            } else {
                reward(2, data, 0.999);
            }
            data.lastPayloadTime = now;
        }
    }

    /**
     * A Derp/HeadLess check.
     * <p>
     * Accuracy: 10/10 - Should not have any false positive.
     * Efficiency: 10/10 - Detects related hacks really fast.
     *
     * @author MrCraftGoo
     */
    private void typeD(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketNode config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.to.pitch > 90.1F || e.to.pitch < -90.1F) {
                // Punish
                this.punish(event, player, data, 3, 6);
            } else {
                reward(3, data, 0.999);
                data.legitLocation = e.to;
            }
        }
    }

    /**
     * An interact check.
     * <p>
     * Accuracy: 9/10 - Has very few false positives, testing required.
     * Efficiency: 9/10 - Has rare occurrences when it stumbles.
     *
     * @author FrozenAnarchy
     */
    private void typeE(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketNode config) {
        // Ignore 1.7 players
        if (player.protocol <= 5) {
            return;
        }
        if ((event instanceof InteractEntityEvent && ((InteractEntityEvent) event).action == InteractEntityEvent.InteractType.ATTACK) ||
                (event instanceof BlockBreakEvent && ((BlockBreakEvent) event).digType == BlockBreakEvent.DigType.COMPLETE) ||
                event instanceof BlockPlaceEvent && ((BlockPlaceEvent) event).placeType == BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
            if (player.isBlocking || player.isPullingBow || player.isEating) {
                this.punish(event, player, data, 4, 5);
            } else {
                this.reward(4, data, 0.99);
            }
        }
    }
}