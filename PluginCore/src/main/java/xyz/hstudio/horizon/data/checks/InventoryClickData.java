package xyz.hstudio.horizon.data.checks;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import xyz.hstudio.horizon.data.Data;

public class InventoryClickData extends Data {

    // TypeA
    public long lastClickTime;
    public int lastRawSlot;
    public Material lastMaterial;
    // TypeB
    public long lastClickOnItem;
    public int typeBFails;
    public Integer buttonClicked;
    public InventoryAction inventoryAction;

    public static double distanceBetweenSlots(final int rawSlotOne, final int rawSlotTwo, final InventoryType inventoryType) {
        double[] locationOfFirstClick = locateSlot(rawSlotOne, inventoryType);
        double[] locationOfSecondClick = locateSlot(rawSlotTwo, inventoryType);

        if (locationOfFirstClick == null || locationOfSecondClick == null) {
            return -1;
        }

        return Math.hypot(locationOfFirstClick[0] - locationOfSecondClick[0], locationOfFirstClick[1] - locationOfSecondClick[1]);
    }

    public static double[] locateSlot(int rawSlot, final InventoryType inventoryType) throws IllegalArgumentException {
        if (rawSlot < 0) {
            return null;
        }
        switch (inventoryType) {
            case CHEST:
            case ENDER_CHEST:
                final double extraYChest = rawSlot < 54 ?
                        (rawSlot < 27 ? 0 : 0.5D)
                        : 0.75D;
                return new double[]{
                        rawSlot % 9,
                        ((rawSlot / 9D) + extraYChest)
                };
            case DISPENSER:
            case DROPPER:
                if (rawSlot < 9) {
                    return new double[]{
                            4 + rawSlot % 3,
                            (rawSlot / 3D)
                    };
                }
                final double extraYDispenser = rawSlot < 36 ? 2.5D : 2.75D;
                return new double[]{
                        rawSlot % 9,
                        extraYDispenser + (rawSlot / 9D)
                };
            case FURNACE:
                switch (rawSlot) {
                    case 0:
                        return new double[]{
                                2.5F,
                                0F
                        };
                    case 1:
                        return new double[]{
                                2.5F,
                                2F
                        };
                    case 2:
                        return new double[]{
                                6F,
                                1F
                        };
                    default:
                        final double extraYFurnace = rawSlot < 30 ? 3.5D : 3.75D;
                        return new double[]{
                                (rawSlot - 3) % 9,
                                extraYFurnace + ((rawSlot - 3D) / 9D)
                        };
                }
            case WORKBENCH:
                if (rawSlot == 0) {
                    return new double[]{
                            6.5D,
                            1D
                    };
                }
                if (rawSlot <= 9) {
                    int xTemp = rawSlot % 3;
                    float yTemp = rawSlot / 3F;
                    return new double[]{
                            (xTemp == 0 ? 3 : xTemp) + 0.25D,
                            yTemp <= 1 ? 0 : (yTemp <= 2 ? 1 : 2)
                    };
                } else {
                    final double extraYWorkbench = rawSlot < 37 ? 2.5D : 2.75D;
                    return new double[]{
                            (rawSlot - 1) % 9,
                            extraYWorkbench + ((rawSlot - 1D) / 9D)
                    };
                }
            case HOPPER:
                if (rawSlot <= 4) {
                    return new double[]{
                            2D + rawSlot,
                            1D
                    };
                }
                rawSlot -= 5;
                final double extraYHopper = rawSlot < 32 ? 2.5D : 2.75D;
                return new double[]{
                        rawSlot % 9,
                        (extraYHopper + (rawSlot / 9D))
                };
            case CRAFTING:
            case PLAYER:
                if (rawSlot == 0) {
                    return new double[]{
                            7.5D,
                            1.5D
                    };
                }
                if (rawSlot <= 4) {
                    return new double[]{
                            5.5D - (rawSlot % 2),
                            rawSlot <= 2 ? 1D : 2D
                    };
                }
                if (rawSlot <= 8) {
                    return new double[]{
                            0D,
                            (rawSlot - 5)
                    };
                }
                final double extraYPlayer = rawSlot < 36 ? 4.25D : 4.5D;
                rawSlot -= 9;
                return new double[]{
                        rawSlot % 9,
                        ((rawSlot / 9D) + extraYPlayer)
                };
            case CREATIVE:
            case ENCHANTING:
            case BEACON:
            case ANVIL:
            case MERCHANT:
            default:
                return null;
        }
    }
}