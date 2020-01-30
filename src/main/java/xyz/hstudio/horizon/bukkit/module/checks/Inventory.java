package xyz.hstudio.horizon.bukkit.module.checks;

import xyz.hstudio.horizon.bukkit.api.ModuleType;
import xyz.hstudio.horizon.bukkit.config.checks.InventoryConfig;
import xyz.hstudio.horizon.bukkit.data.HoriPlayer;
import xyz.hstudio.horizon.bukkit.data.checks.InventoryData;
import xyz.hstudio.horizon.bukkit.module.Module;
import xyz.hstudio.horizon.bukkit.network.events.Event;
import xyz.hstudio.horizon.bukkit.network.events.inbound.*;
import xyz.hstudio.horizon.bukkit.network.events.outbound.CloseWindowEvent;

public class Inventory extends Module<InventoryData, InventoryConfig> {

    public Inventory() {
        super(ModuleType.Inventory, new InventoryConfig());
    }

    @Override
    public InventoryData getData(final HoriPlayer player) {
        return player.inventoryData;
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InventoryData data, final InventoryConfig config) {
        typeA(event, player, data, config);
    }

    /**
     * Basic Inventory check.
     * <p>
     * Accuracy: 10/10 - There should be 0 false positives.
     * Efficiency: 10/10 - Detects most Inventory related hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final InventoryData data, final InventoryConfig config) {
        if (event instanceof ClientCommandEvent) {
            ClientCommandEvent e = (ClientCommandEvent) event;
            // In 1.8.8 or lower, client will send this packet when opening player inventory.
            // However, this won't work in 1.9+.
            if (e.command == ClientCommandEvent.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                data.inventoryOpened = true;
                data.inventoryOpenTick = player.currentTick;
            }
        } else if (event instanceof WindowClickEvent) {
            // Client clicked window, so there should be a window opened.
            data.inventoryOpened = true;
            data.inventoryOpenTick = player.currentTick;
        } else if (event instanceof WindowCloseEvent) {
            // Client closed window
            data.inventoryOpened = false;
        } else if (event instanceof CloseWindowEvent) {
            // Server closed window
            data.inventoryOpened = false;
        } else if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!data.inventoryOpened || e.isTeleport) {
                return;
            }

            // Client still sends rotation/position packet after 1~3 tick player open inventory
            // if player open inventory while moving/rotating

            // Allowing 21 ticks to avoid false positives
            if (player.currentTick - data.inventoryOpenTick < 21) {
                return;
            }

            // It's impossible to rotate if inventory is opened.
            if (config.typeA_checkRotation && e.updateRot) {
                // Block Inventory Rotation
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 5);
            }

            if (e.updatePos) {
                // TODO: Inventory Position check
                // Inventory Position check is harder to make
                // because player can get velocity/push even if inventory is opened.
            }
        } else if (event instanceof ActionEvent) {
            if (!data.inventoryOpened) {
                return;
            }
            // Block Inventory Sprint/Sneak/Glide
            if (config.typeA_checkAction) {
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 5);
            }
        } else if (event instanceof InteractEntityEvent) {
            if (!data.inventoryOpened) {
                return;
            }
            if (config.typeA_checkHit) {
                // Block Inventory Hit
                this.debug("Failed: TypeA");

                // Punish
                this.punish(player, data, "TypeA", 5);
            }
        }
    }
}