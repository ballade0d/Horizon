package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.CustomPayloadEvent;
import xyz.hstudio.horizon.api.events.inbound.KeepAliveRespondEvent;
import xyz.hstudio.horizon.api.events.inbound.MoveEvent;
import xyz.hstudio.horizon.config.checks.BadPacketConfig;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.BadPacketData;
import xyz.hstudio.horizon.module.Module;
import xyz.hstudio.horizon.thread.Sync;

public class BadPacket extends Module<BadPacketData, BadPacketConfig> {

    public BadPacket() {
        super(ModuleType.BadPacket, new BadPacketConfig());
    }

    @Override
    public BadPacketData getData(final HoriPlayer player) {
        return player.badPacketData;
    }

    @Override
    public void cancel(final Event event, final String type, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        if (type.equals("TypeB") || type.equals("TypeC")) {
            event.setCancelled(true);
        } else if (type.equals("TypeD")) {
            event.setCancelled(true);
            Sync.teleport(player, data.legitLocation);
        }
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
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
    }

    /**
     * A FreeCam/Freeze check. It can detect players from not sending movement packet.
     * <p>
     * Accuracy: 10/10 - Should not have any false positive.
     * Efficiency: 8/10 - Detects related hacks fast.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        if (event instanceof KeepAliveRespondEvent) {
            // A player should send 20 move packets(1.8.8 or lower)
            // or 1 move packet (1.9 or higher) at least in one second.
            // If player didn't send move packets in 2 sec (KeepAlive Interval)
            // but send KeepAlive packets, the player is definitely cheating.
            if (player.currentTick == data.lastTick) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(event, player, data, "TypeA", 5);
            } else {
                reward("TypeA", data, 0.99);
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
    private void typeB(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.moveType != MoveEvent.MoveType.FLYING || e.isTeleport) {
                data.flyingCount = 0;
                return;
            }
            // A player can send only 19 flying packets in a row.
            if (++data.flyingCount > 20) {
                this.debug("Failed: TypeB");

                // Punish
                this.punish(event, player, data, "TypeB", 5);
            } else if (data.flyingCount == 0) {
                reward("TypeB", data, 0.99);
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
    private void typeC(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        if (event instanceof CustomPayloadEvent) {
            CustomPayloadEvent e = (CustomPayloadEvent) event;

            String brand = e.brand;
            // Only checks for these 2 brands
            if (!brand.equalsIgnoreCase("MC|BEdit") &&
                    !brand.equalsIgnoreCase("MC|BSign")) {
                return;
            }
            long now = System.currentTimeMillis();
            if (now - data.lastPayloadTime <= e.length) {
                this.debug("Failed: TypeC");

                // Punish
                this.punish(event, player, data, "TypeC", 5);
            } else {
                reward("TypeC", data, 0.999);
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
    private void typeD(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.to.pitch > 90.1F || e.to.pitch < -90.1F) {
                this.debug("Failed: TypeD");

                // Punish
                this.punish(event, player, data, "TypeD", 6);
            } else {
                reward("TypeD", data, 0.999);
                data.legitLocation = e.to;
            }
        }
    }
}