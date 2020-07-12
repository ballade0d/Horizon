package xyz.hstudio.horizon.module.checks;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import xyz.hstudio.horizon.api.ModuleType;
import xyz.hstudio.horizon.compat.McAccessor;
import xyz.hstudio.horizon.data.HoriPlayer;
import xyz.hstudio.horizon.data.checks.InventoryClickData;
import xyz.hstudio.horizon.events.Event;
import xyz.hstudio.horizon.events.inbound.SyncWindowClickEvent;
import xyz.hstudio.horizon.events.inbound.WindowClickEvent;
import xyz.hstudio.horizon.events.inbound.WindowCloseEvent;
import xyz.hstudio.horizon.file.node.InventoryClickNode;
import xyz.hstudio.horizon.module.Module;

public class InventoryClick extends Module<InventoryClickData, InventoryClickNode> {

    public InventoryClick() {
        super(ModuleType.InventoryClick, new InventoryClickNode(), "TypeA", "TypeB", "TypeC");
    }

    @Override
    public InventoryClickData getData(final HoriPlayer player) {
        return player.inventoryClickData;
    }

    @Override
    public void cancel(final Event event, final int type, final HoriPlayer player, final InventoryClickData data, final InventoryClickNode config) {
        event.setCancelled(true);
    }

    @Override
    public void doCheck(final Event event, final HoriPlayer player, final InventoryClickData data, final InventoryClickNode config) {
        if (config.typeA_enabled) {
            typeA(event, player, data, config);
        }
        if (config.typeB_enabled) {
            typeB(event, player, data, config);
        }
        if (config.typeC_enabled) {
            typeC(event, player, data, config);
        }
        if (event instanceof SyncWindowClickEvent) {
            SyncWindowClickEvent e = (SyncWindowClickEvent) event;
            data.lastClickTime = System.currentTimeMillis();
            data.lastRawSlot = e.rawSlot;
            data.lastMaterial = e.getCurrentItem() == null ? Material.AIR : e.getCurrentItem().getType();
            if (e.getCurrentItem() != null) {
                data.lastClickOnItem = System.currentTimeMillis();
            }
        }
    }

    private void typeA(final Event event, final HoriPlayer player, final InventoryClickData data, final InventoryClickNode config) {
        if (event instanceof SyncWindowClickEvent) {
            SyncWindowClickEvent e = (SyncWindowClickEvent) event;
            if (player.getPlayer().getGameMode() != GameMode.SURVIVAL &&
                    player.getPlayer().getGameMode() != GameMode.ADVENTURE) {
                return;
            }
            if (McAccessor.INSTANCE.getPing(player.getPlayer()) > 300) {
                return;
            }
            if (e.rawSlot == data.lastRawSlot) {
                return;
            }
            int addedVl = 3;
            int enforcedTicks = 0;
            switch (e.action) {
                case NOTHING:
                case UNKNOWN:
                case COLLECT_TO_CURSOR:
                    return;
                case HOTBAR_SWAP:
                case HOTBAR_MOVE_AND_READD:
                    addedVl = 1;
                    enforcedTicks = 1;
                    if (InventoryClickData.distanceBetweenSlots(e.rawSlot, data.lastRawSlot, e.clickedInventory.getType()) >= 3) {
                        return;
                    }
                    break;

                case DROP_ALL_SLOT:
                case DROP_ONE_SLOT:
                    enforcedTicks = 1;
                    break;
                case PICKUP_ALL:
                case PICKUP_SOME:
                case PICKUP_HALF:
                case PICKUP_ONE:
                case PLACE_ALL:
                case PLACE_SOME:
                case PLACE_ONE:
                    addedVl = 3;
                    enforcedTicks = (InventoryClickData.distanceBetweenSlots(e.rawSlot, data.lastRawSlot, e.clickedInventory.getType()) < 4) ?
                            1 :
                            5;
                    break;
                case DROP_ALL_CURSOR:
                case DROP_ONE_CURSOR:
                case CLONE_STACK:
                    enforcedTicks = 2;
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                    if (data.lastMaterial == e.getCurrentItem().getType()) {
                        return;
                    }
                    enforcedTicks = (InventoryClickData.distanceBetweenSlots(e.rawSlot, data.lastRawSlot, e.clickedInventory.getType()) < 4) ?
                            1 :
                            2;
                    break;
                case SWAP_WITH_CURSOR:
                    switch (e.slotType) {
                        case FUEL:
                        case RESULT:
                            enforcedTicks = 4;
                            break;
                        default:
                            enforcedTicks = 2;
                            break;
                    }
                    break;
            }

            int expect = 25 + enforcedTicks * 50;
            long delta = System.currentTimeMillis() - data.lastClickTime;
            if (delta < expect) {
                this.punish(e, player, data, 1, addedVl, "d:" + delta, "e:" + expect);
            }
        }
    }

    private void typeB(final Event event, final HoriPlayer player, final InventoryClickData data, final InventoryClickNode config) {
        if (event instanceof WindowCloseEvent) {
            WindowCloseEvent e = (WindowCloseEvent) event;
            if (player.getPlayer().getGameMode() != GameMode.SURVIVAL &&
                    player.getPlayer().getGameMode() != GameMode.ADVENTURE) {
                return;
            }
            if (McAccessor.INSTANCE.getPing(player.getPlayer()) > 300) {
                return;
            }
            if (!isInventoryEmpty(e.inventory)) {
                return;
            }
            long passedTime = System.currentTimeMillis() - data.lastClickOnItem;
            if (passedTime <= 70) {
                if (++data.typeBFails > 4) {
                    this.punish(e, player, data, 2, 5, "t:" + passedTime);
                }
            } else if (data.typeBFails > 0) {
                data.typeBFails--;
            }
        }
    }

    /**
     * Button check.
     * <p>
     * Accuracy: 10/10 - Haven't found any false positives.
     * Efficiency: 10/10 - Detects on first item drop.
     *
     * @author FrozenAnarchy
     */
    private void typeC(final Event event, final HoriPlayer player, final InventoryClickData data, final InventoryClickNode config) {
        if (!(event instanceof WindowClickEvent) && !(event instanceof SyncWindowClickEvent)) {
            return;
        }
        if (event instanceof WindowClickEvent) {
            data.buttonClicked = ((WindowClickEvent) event).button;
        } else {
            data.inventoryAction = ((SyncWindowClickEvent) event).action;
        }

        if (data.inventoryAction == null || data.buttonClicked == Integer.MAX_VALUE) {
            return;
        }

        if ((data.inventoryAction.equals(InventoryAction.UNKNOWN) || isActionDrop(data.inventoryAction)) && data.buttonClicked > 2) {
            // can't drop item using button > 2
            // LiquidBounce uses UNKNOWN to drop items
            data.buttonClicked = Integer.MAX_VALUE;
            data.inventoryAction = null;
            this.punish(event, player, data, 2, 5);
        }
    }

    private boolean isInventoryEmpty(final org.bukkit.inventory.Inventory inventory) {
        for (ItemStack content : inventory.getContents()) {
            if (content != null) {
                return false;
            }
        }
        return true;
    }

    private boolean isActionDrop(final InventoryAction action) {
        if (action == null) {
            return false;
        }

        switch (action) {
            case DROP_ALL_CURSOR:
            case DROP_ALL_SLOT:
            case DROP_ONE_CURSOR:
            case DROP_ONE_SLOT:
                return true;
            default:
                return false;
        }
    }
}