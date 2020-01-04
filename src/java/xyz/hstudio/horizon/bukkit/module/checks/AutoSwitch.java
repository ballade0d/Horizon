package xyz.hstudio.horizon.bukkit.module.checks;

import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.AutoSwitchConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.AutoSwitchData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.BlockPlaceEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.HeldItemEvent;
import xyz.hstudio.horizon.bukkit.network.events.inbound.InteractItemEvent;

public class AutoSwitch extends Module<AutoSwitchData, AutoSwitchConfig> {

    public AutoSwitch() {
        super(ModuleType.AutoSwitch, new AutoSwitchConfig());
    }

    @Override
    public AutoSwitchData getData(final HoriPlayer player) {
        return player.autoSwitchData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final AutoSwitchData data, final AutoSwitchConfig config) {
        typeA(event, player, data, config);
        typeB(event, player, data, config);
    }

    /**
     * A Scaffold(AutoBlock/AutoSwitch) check.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positive.
     * Efficiency: 10/10 - Detects most autoswitch instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final AutoSwitchData data, final AutoSwitchConfig config) {
        // TODO: Remember to skip 1.9+ client.
        if (event instanceof BlockPlaceEvent) {
            BlockPlaceEvent e = (BlockPlaceEvent) event;
            if (e.placeType != BlockPlaceEvent.PlaceType.PLACE_BLOCK) {
                return;
            }
            data.lastPlaceTick = player.currentTick;
        } else if (event instanceof HeldItemEvent) {
            if (player.currentTick - data.lastPlaceTick <= 1) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 0);
            }
        }
    }

    /**
     * An AutoSoup/AutoUse check.
     * <p>
     * Accuracy: 10/10 - It shouldn't have any false positive.
     * Efficiency: 10/10 - Detects most autouse instantly.
     *
     * @author MrCraftGoo
     */
    private void typeB(final Event event, final HoriPlayer player, final AutoSwitchData data, final AutoSwitchConfig config) {
        // TODO: Remember to skip 1.9+ client.
        if (event instanceof InteractItemEvent) {
            data.usingItem = true;
        } else if (event instanceof HeldItemEvent) {
            long deltaT = player.currentTick - data.lastSwitchTick;
            if (data.usingItem && deltaT < 2) {
                this.debug("Failed: TypeB, t:" + deltaT);

                // Punish
                this.punish(player, data, "TypeB", 0);
            }
            data.usingItem = false;
            data.lastSwitchTick = player.currentTick;
        }
    }
}