package xyz.hstudio.horizon.module.checks;

import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.api.events.Event;
import xyz.hstudio.horizon.api.events.inbound.*;
import xyz.hstudio.horizon.api.events.outbound.CloseWindowEvent;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.InventoryData;
import xyz.hstudio.horizon.file.node.InventoryNode;
import xyz.hstudio.horizon.module.Module;

public class Inventory extends Module<InventoryData, InventoryNode> {

    public Inventory() {
        super(ModuleType.Inventory, new InventoryNode(), "TypeA");
    }

    @Override
    public InventoryData getData(final HoriPlayer player) {
        return player.inventoryData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final InventoryData data, final InventoryNode config) {
        McAccessor.INSTANCE.ensureMainThread(() -> player.player.closeInventory());
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InventoryData data, final InventoryNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
    }

    /**
     * Basic Inventory check.
     * <p>
     * Accuracy: 10/10 - There should be 0 false positives.
     * Efficiency: 10/10 - Detects most Inventory related hacks instantly.
     *
     * @author MrCraftGoo
     */
    private void typeA(final Event event, final HoriPlayer player, final InventoryData data, final InventoryNode config) {
        if (event instanceof ClientCommandEvent) {
            ClientCommandEvent e = (ClientCommandEvent) event;
            // In 1.8.8 or lower, client will send this packet when opening player inventory.
            // However, this won't work in 1.9+.
            if (e.command == ClientCommandEvent.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                data.inventoryOpened = data.temporarilyBypass = true;
                data.inventoryOpenTick = player.currentTick;
                data.legitLocation = player.position;
            }
        } else if (event instanceof WindowClickEvent) {
            // Client clicked window, so there should be a window opened.
            if (!data.inventoryOpened) {
                data.temporarilyBypass = true;
            }
            data.inventoryOpened = true;
            data.inventoryOpenTick = player.currentTick;
            data.legitLocation = player.position;
        } else if (event instanceof WindowCloseEvent) {
            // Client closed window
            data.inventoryOpened = false;
        } else if (event instanceof CloseWindowEvent) {
            // Server closed window
            data.inventoryOpened = false;
        } else if (event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            if (!data.inventoryOpened || e.isTeleport || (!e.hasDeltaPos() && !e.hasDeltaRot())) {
                return;
            }

            // It's impossible to rotate if inventory is opened.
            if (config.typeA_checkRotation && e.hasDeltaRot() && player.currentTick - data.inventoryOpenTick >= 21) {
                // Block Inventory Rotation

                // Punish
                this.punish(event, player, data, 0, 3, "t:rot");
            }

            if (e.knockBack != null) {
                data.temporarilyBypass = true;
            } else if (e.onGround && player.onGroundReally && data.temporarilyBypass) {
                data.temporarilyBypass = false;
                data.inventoryOpenTick = player.currentTick;
                return;
            }

            // Client still sends rotation/position packet after 1~3 tick player open inventory
            // if player open inventory while moving/rotating

            // Allowing 21 ticks to avoid false positives
            if (player.currentTick - data.inventoryOpenTick < 21) {
                return;
            }
            if (config.typeA_checkPosition && e.hasDeltaPos() && e.velocity.length() > 0.1 &&
                    !e.isInLiquid && !data.temporarilyBypass && !e.isCollidingEntities) {
                // Block Inventory Position

                // Punish
                this.punish(event, player, data, 0, 3, "t:pos", "l:" + e.velocity.length());
            }
        } else if (event instanceof ActionEvent) {
            ActionEvent e = (ActionEvent) event;
            if (!data.inventoryOpened) {
                return;
            }
            if (config.typeA_checkAction && (e.action == ActionEvent.Action.START_SNEAKING ||
                    e.action == ActionEvent.Action.START_SPRINTING || e.action == ActionEvent.Action.START_GLIDING)) {
                // Block Inventory Sprint/Sneak/Glide

                // Punish
                this.punish(event, player, data, 0, 5, "t:action");
            }
        } else if (event instanceof InteractEntityEvent) {
            if (!data.inventoryOpened) {
                return;
            }
            if (config.typeA_checkHit) {
                // Block Inventory Hit

                // Punish
                this.punish(event, player, data, 0, 5, "t:hit");
            }
        }
    }
}