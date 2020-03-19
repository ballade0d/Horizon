package xyz.hstudio.horizon.util.enums;

import org.bukkit.Material;

public enum MatUtils {

    COCOA_BEANS("COCOA"),
    CHORUS_FLOWER(),
    CHORUS_PLANT(),
    BAMBOO(),
    END_ROD(),
    FARMLAND("SOIL"),
    ENCHANTED_GOLDEN_APPLE("GOLDEN_APPLE"),
    SLIME_BLOCK("SLIME_BLOCK"),
    LADDER("LADDER"),
    VINE("VINE"),
    SEA_PICKLE(),
    SCAFFOLDING(),
    REPEATER("DIODE"),
    DIODE_BLOCK_ON(),
    DIODE_BLOCK_OFF(),
    COMPARATOR("REDSTONE_COMPARATOR"),
    REDSTONE_COMPARATOR_ON(),
    REDSTONE_COMPARATOR_OFF(),
    LILY_PAD("WATER_LILY"),
    COBWEB("WEB");

    private final String[] legacy;

    MatUtils(final String... legacy) {
        this.legacy = legacy;
    }

    public static boolean isLiquid(final Material material) {
        return material.name().contains("WATER") || material.name().contains("LAVA");
    }

    public Material parse() {
        Material newMat = Material.getMaterial(this.name());
        if (newMat != null) {
            return newMat;
        }
        Material oldMat;
        for (int i = this.legacy.length - 1; i >= 0; i--) {
            oldMat = Material.getMaterial(this.legacy[i]);
            if (oldMat != null) {
                return oldMat;
            }
        }
        return null;
    }
}