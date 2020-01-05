package xyz.hstudio.horizon.bukkit.util;

import org.bukkit.Material;

public enum MaterialUtils {

    SLIME_BLOCK(),
    HONEY_BLOCK(),
    COBWEB("WEB");

    private final String[] legacy;

    MaterialUtils(final String... legacy) {
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