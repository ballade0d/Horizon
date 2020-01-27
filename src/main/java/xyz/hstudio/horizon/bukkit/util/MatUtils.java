package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Material;

public enum MatUtils {

    COCOA_BEANS("INK_SACK", "COCOA"),
    CHORUS_FLOWER(),
    CHORUS_PLANT(),
    BAMBOO(),
    END_ROD(),
    FARMLAND("SOIL"),
    SLIME_BLOCK("SLIME_BLOCK"),
    LADDER("LADDER"),
    VINE("VINE"),
    SEA_PICKLE(),
    SCAFFOLDING(),
    COMPARATOR("REDSTONE_COMPARATOR", "REDSTONE_COMPARATOR_ON", "REDSTONE_COMPARATOR_OFF"),
    LILY_PAD("WATER_LILY"),
    REPEATER("DIODE", "DIODE_BLOCK_ON", "DIODE_BLOCK_OFF"),
    COBWEB("WEB");

    private final String[] legacy;

    MatUtils(final String... legacy) {
        this.legacy = legacy;
    }

    public Material parse() {
        Material newMat = Material.getMaterial(this.name());
        if (newMat != null && (Version.VERSION == Version.v1_13_R2
                || Version.VERSION == Version.v1_14_R1 || Version.VERSION == Version.v1_15_R1)) {
            return newMat;
        }
        Material oldMat;
        for (int i = this.legacy.length - 1; i >= 0; i--) {
            String legacyName = this.legacy[i];
            if (legacyName.contains("/")) {
                oldMat = Material.getMaterial(legacyName);
                if (oldMat != null) {
                    return oldMat;
                } else {
                    continue;
                }
            }
            oldMat = Material.getMaterial(legacyName);
            if (oldMat != null) {
                return oldMat;
            }
        }
        return null;
    }
}