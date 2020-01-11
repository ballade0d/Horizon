package xyz.hstudio.horizon.bukkit.module.checks;

import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.BadPacketConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.BadPacketData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.KeepAliveRespondEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.MoveEvent;

public class BadPacket extends Module<BadPacketData, BadPacketConfig> {

    public BadPacket() {
        super(ModuleType.BadPacket, new BadPacketConfig());
    }

    @Override
    public BadPacketData getData(final HoriPlayer player) {
        return player.badPacketData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        typeA(event, player, data, config);
        typeB(event, player, data, config);
    }

    /**
     * A FreeCam/Freeze check. It can detect players from not sending movement packet.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        if (event instanceof KeepAliveRespondEvent) {
            // A player should send 20 move packets(1.8.8 or lower)
            // or 1 move packet (1.9 or higher) at least in one second.
            if (player.currentTick == data.lastTick) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 0);
            }
            data.lastTick = player.currentTick;
        }
    }

    /**
     * A Paralyze/MoveExploit check.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final BadPacketData data, final BadPacketConfig config) {
        if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (e.moveType != MoveEvent.MoveType.FLYING) {
                data.flyingCount = 0;
                return;
            }
            // A player can send only 19 flying packets in a row.
            if (++data.flyingCount > 20) {
                this.debug("Failed: TypeB");

                // Punish
                this.punish(player, data, "TypeB", 0);
            }
        }
    }
}